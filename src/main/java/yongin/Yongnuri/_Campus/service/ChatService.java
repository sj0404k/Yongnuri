package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.admin.AdminConfig;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.dto.Notificationres;
import yongin.Yongnuri._Campus.dto.chat.*;
import yongin.Yongnuri._Campus.exception.ResourceNotFoundException;
import yongin.Yongnuri._Campus.repository.*;
import yongin.Yongnuri._Campus.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatStatusRepository chatStatusRepository;
    private final UsedItemRepository usedItemRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    private final AdminConfig adminConfig;
    private final ImageRepository imageRepository;

    /**
     * [수정] N+1 문제를 해결하여 성능을 대폭 개선한 채팅방 목록 조회 메소드
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRooms(CustomUserDetails user, Enum.ChatType type) {
        // 1. 현재 유저가 참여중인 활성화된 채팅방 ID 목록을 가져온다.
        List<ChatStatus> activeStatuses = chatStatusRepository.findByUserIdAndChatStatusTrue(user.getUser().getId());
        if (activeStatuses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> activeRoomIds = activeStatuses.stream()
                .map(cs -> cs.getChatRoom().getId())
                .toList();

        // 2. 채팅방 정보와 참여자 정보를 한번의 쿼리로 가져온다. (JOIN FETCH)
        Enum.ChatType chatType = (type != null) ? type : Enum.ChatType.ALL;
        List<ChatRoom> rooms = (chatType == Enum.ChatType.ALL)
                ? chatRoomRepository.findByIdInWithParticipants(activeRoomIds)
                : chatRoomRepository.findByIdInAndTypeWithParticipants(activeRoomIds, chatType);

        // 3. 모든 채팅방의 마지막 메시지를 한번의 쿼리로 가져온다.
        Map<Long, ChatMessages> lastMessagesMap = chatMessagesRepository.findLastMessagesByRoomIds(activeRoomIds)
                .stream()
                .collect(Collectors.toMap(msg -> msg.getChatRoom().getId(), Function.identity()));

        // 4. DTO로 변환 (이제 추가 쿼리 발생 없음)
        return rooms.stream()
                .map(room -> {
                    User opponentUser = room.getParticipants().stream()
                            .map(ChatStatus::getUser)
                            .filter(u -> !u.getId().equals(user.getUser().getId()))
                            .findFirst()
                            .orElse(null); // 관리자와의 채팅 등 상대방이 없는 경우를 대비

                    ChatMessages lastMessage = lastMessagesMap.get(room.getId());
                    return ChatRoomDto.fromEntity(room, opponentUser, lastMessage);
                })
                .collect(Collectors.toList());
    }

    /**
     * [수정] 비관적 락(Pessimistic Lock)을 적용하여 동시성 문제를 해결하고, 논리적 오류를 수정한 채팅방 생성 메소드
     */
    @Transactional
    public ChatEnterRes createChatRoom(CustomUserDetails user, ChatRoomReq request) {
        // 1. 비관적 락을 사용하여 다른 트랜잭션의 동시 접근을 막고, 참여자 정보를 함께 조회 (N+1 방지)
        List<ChatRoom> existingRooms = chatRoomRepository.findByTypeAndTypeIdWithParticipantsAndLock(
                request.getType(), request.getTypeId());

        // 2. 참여자(현재 유저, 요청받은 toUser)가 동일한 방이 있는지 체크
        Optional<ChatRoom> existing = existingRooms.stream()
                .filter(room -> {
                    List<Long> participantIds = room.getParticipants().stream()
                            .map(p -> p.getUser().getId())
                            .toList();
                    return participantIds.contains(user.getUser().getId()) && participantIds.contains(request.getToUserId());
                })
                .findFirst();

        // 채팅 상대방(toUser) 조회
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅 상대를 찾을 수 없습니다."));

        if (existing.isPresent()) {
            // 3-1. 이미 방이 존재하면 해당 방으로 입장
            log.info("Existing room found {}. Entering.", existing.get().getId());
            return getEnterChatRoom(user, existing.get().getId());
        } else {
            // 3-2. 방이 없으면 새로 생성 (락이 걸려있어 안전함)
            log.info("No existing room. Creating new one for post {} with user {}", request.getTypeId(), toUser.getId());

            // 본인에게 채팅을 거는 경우 방지 (명확하게 toUser와 비교)
            if (toUser.getId().equals(user.getUser().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인에게는 채팅을 보낼 수 없습니다.");
            }

            // 게시글 존재 여부 및 유효성 검증 (게시글 작성자 정보는 'targetUser'로 받음)
            // ADMIN 채팅이 아닐 경우, 요청받은 toUser가 실제 게시글 작성자인지 확인
            if (request.getType() != Enum.ChatType.ADMIN) {
                User targetUser;
                switch (request.getType()) {
                    case LOST_ITEM -> {
                        LostItem lostItem = lostItemRepository.findById(request.getTypeId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "분실물 게시글을 찾을 수 없습니다."));
                        targetUser = lostItem.getUser();
                    }
                    case USED_ITEM -> {
                        UsedItem usedItem = usedItemRepository.findById(request.getTypeId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "중고거래 게시글을 찾을 수 없습니다."));
                        targetUser = userRepository.findById(usedItem.getUserId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "해당 게시글 작성자를 찾을 수 없습니다."));
                    }
                    case GROUP_BUY -> {
                        GroupBuy groupBuy = groupBuyRepository.findById(request.getTypeId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공동구매 게시글을 찾을 수 없습니다."));
                        targetUser = userRepository.findById(groupBuy.getUserId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "해당 게시글 작성자를 찾을 수 없습니다."));
                    }
                    case ADMIN -> {
                        targetUser = userRepository.findByEmail(adminConfig.getEmail())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관리자 계정을 찾을 수 없습니다."));
                    }
                    default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 채팅 타입입니다.");
                }
                if (!targetUser.getId().equals(toUser.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시글 작성자가 아닌 사용자에게 채팅을 요청했습니다.");
                }
            }

            // 채팅방 생성
            ChatRoom newChatRoom = ChatRoom.builder()
                    .type(request.getType())
                    .typeId(request.getTypeId())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .status(ChatRoom.RoomStatus.ACTIVE)
                    .build();
            chatRoomRepository.save(newChatRoom);

            // 첫 메시지 저장
            if (request.getMessageType() == ChatMessages.messageType.text && request.getMessage() != null) {
                ChatMessages newChatMessages = ChatMessages.builder()
                        .chatRoom(newChatRoom)
                        .chatType(request.getMessageType())
                        .message(request.getMessage())
                        .sender(user.getUser())
                        .createdAt(LocalDateTime.now())
                        .build();
                chatMessagesRepository.save(newChatMessages);
            }

            // 참여자(ChatStatus) 생성
            ChatStatus myStatus = ChatStatus.builder()
                    .chatRoom(newChatRoom)
                    .user(user.getUser())
                    .firstDate(LocalDateTime.now())
                    .lastDate(LocalDateTime.now())
                    .chatStatus(true)
                    .build();

            // [논리 오류 수정] 상대방을 targetUser가 아닌 명확한 toUser로 설정
            ChatStatus opponentStatus = ChatStatus.builder()
                    .chatRoom(newChatRoom)
                    .user(toUser)
                    .firstDate(LocalDateTime.now())
                    .lastDate(LocalDateTime.now())
                    .chatStatus(true)
                    .build();

            chatStatusRepository.saveAll(List.of(myStatus, opponentStatus));
            return getEnterChatRoom(user, newChatRoom.getId());
        }
    }


    @Transactional
    public ChatEnterRes getEnterChatRoom(CustomUserDetails user, Long roomId) {
        // 1. 채팅방 조회
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 2. 해당 방의 참여자 조회
        List<ChatStatus> participants = chatStatusRepository.findByChatRoomId(roomId);

        // 3. 현재 유저가 참여자인지 확인
        participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getUser().getId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("이 채팅방에 접근할 권한이 없습니다."));

        // 4. 상대방 찾기
        User opponent = participants.stream()
                .map(ChatStatus::getUser)
                .filter(u -> !u.getId().equals(user.getUser().getId()))
                .findFirst()
                .orElse(null); // 상대방이 나간 경우 등 예외상황 처리

        // 5. 메시지 목록 (최근순 정렬)
        List<ChatMessages> messageList = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        // 6. 마지막 접속 시간 업데이트
        participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getUser().getId()))
                .findFirst()
                .ifPresent(status -> {
                    status.setLastDate(LocalDateTime.now());
                    chatStatusRepository.save(status);
                });

        // 7. 게시글 등 추가 정보 조회
        Object extraInfo = null;
        String thumbnailUrl = null;
        switch (room.getType()) {
            case LOST_ITEM -> lostItemRepository.findById(room.getTypeId()).ifPresent(item -> {
                if (Boolean.TRUE.equals(item.getIsImages())) {
                    // thumbnailUrl 설정 로직...
                }
            });
            case USED_ITEM -> {
                UsedItem used = usedItemRepository.findById(room.getTypeId()).orElse(null);
                extraInfo = used;

                if (used != null && Boolean.TRUE.equals(used.getIsImages())) {
                    thumbnailUrl = imageRepository
                            .findByTypeAndTypeIdInAndSequence("USED_ITEM", List.of(used.getId()), 1)
                            .stream()
                            .findFirst()
                            .map(Image::getImageUrl)
                            .orElse(null);
                }
            }
            case GROUP_BUY -> groupBuyRepository.findById(room.getTypeId()).ifPresent(item -> {
                if (Boolean.TRUE.equals(item.getIsImages())) {
                    // thumbnailUrl 설정 로직...
                }
            });
            case ADMIN -> extraInfo = userRepository.findByEmail(adminConfig.getEmail())
                    .map(User::getText)
                    .orElse("**채팅 공지사항**");
        }

        // 8. DTO로 반환
        return ChatEnterRes.from(room, opponent, messageList, extraInfo, thumbnailUrl);
    }

    @Transactional
    public void deleteChatRoom(CustomUserDetails user, Long chatRoomId) {
        ChatStatus chatStatus = chatStatusRepository.findByUserIdAndChatRoomId(
                user.getUser().getId(), chatRoomId);
        if (chatStatus == null) {
            throw new IllegalArgumentException("해당 채팅방에 대한 참여 정보를 찾을 수 없습니다.");
        }
        chatStatus.setChatStatus(false);
        chatStatusRepository.save(chatStatus);
    }

    @Transactional
    public void updateTradeStatus(CustomUserDetails user, Long roomId, Enum.UsedItemStatus newStatus) {
        User currentUser = user.getUser();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        if (chatRoom.getType() != Enum.ChatType.USED_ITEM) {
            throw new IllegalArgumentException("거래 상태를 변경할 수 없는 종류의 채팅방입니다.");
        }

        Long usedItemId = chatRoom.getTypeId();
        UsedItem usedItem = usedItemRepository.findById(usedItemId)
                .orElseThrow(() -> new ResourceNotFoundException("연결된 중고거래 게시글을 찾을 수 없습니다. ID: " + usedItemId));

        if (!usedItem.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("거래 상태를 변경할 권한이 없습니다.");
        }
        usedItem.setStatus(newStatus);
        usedItemRepository.save(usedItem); // 명시적으로 save 호출

        String notificationMessage = "판매자가 상품 상태를 '" + newStatus + "'(으)로 변경했습니다.";
        Notificationres notification = Notificationres.builder()
                .chatType(Enum.ChatType.Chat) // 시스템 메시지 타입 정의가 필요할 수 있음
                .typeId(chatRoom.getId())
                .message(notificationMessage)
                .build();
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMessage(CustomUserDetails user, ChatMessageRequest message) {
        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("메시지를 보낼 채팅방을 찾을 수 없습니다."));

        ChatMessages newChatMessages = ChatMessages.builder()
                .chatRoom(chatRoom)
                .chatType(message.getType())
                .message(message.getMessage())
                .sender(user.getUser())
                .createdAt(LocalDateTime.now())
                .build();

        chatMessagesRepository.save(newChatMessages);

        // (선택) 웹소켓으로 메시지 전송 로직 추가
    }
}