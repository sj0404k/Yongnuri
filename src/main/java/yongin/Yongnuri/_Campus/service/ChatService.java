package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatStatusRepository chatStatusRepository;
    private static final int MESSAGE_PAGE_SIZE = 20;// 채팅 20개 씩 보여주기
    private final UsedItemRepository usedItemRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    private final AdminConfig adminConfig;
    // 첫 채팅방들 모음

    /** 필요한 것
     * 닉네임
     * 시간
     * 내용
     */
    public List<ChatRoomDto> getChatRooms(CustomUserDetails user, Enum.ChatType type) {

        Enum.ChatType chatType = (type != null) ? type : Enum.ChatType.ALL;
        // 유저의 활성화된 채팅 상태 조회
        List<ChatStatus> activeStatuses = chatStatusRepository.findByUserIdAndChatStatusTrue(user.getUser().getId());

        // 비어있으면 빈 리스트 반환
        if (activeStatuses.isEmpty()) {
            return Collections.emptyList();
        }
        // 활성화된 채팅방 ID 추출
        List<Long> activeRoomIds = activeStatuses.stream()
                .map(cs -> cs.getChatRoom().getId())
                .toList();
        // 채팅 타입에 따른 필터링
        List<ChatRoom> rooms;
        if (chatType == Enum.ChatType.ALL) {
            rooms = chatRoomRepository.findByIdIn(activeRoomIds);
        } else {
            rooms = chatRoomRepository.findByIdInAndType(activeRoomIds, chatType);
        }
        // DTO 변환
        return rooms.stream()
                .map(room -> {
                    // 마지막 메시지 조회 (가장 최근 메시지 1개)
                    ChatMessages lastMessage = chatMessagesRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                            .orElse(null);
                    // 해당 채팅방의 모든 참여자 상태 가져오기
                    List<ChatStatus> participants = chatStatusRepository.findByChatRoomId(room.getId());

                    // 현재 로그인한 유저 제외 → 상대방 찾기
                    User opponentUser = participants.stream()
                            .map(ChatStatus::getUser)
                            .filter(u -> !u.getId().equals(user.getUser().getId()))
                            .findFirst()
                            .orElse(null);
                    return ChatRoomDto.fromEntity(room,opponentUser ,lastMessage);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatEnterRes createChatRoom(CustomUserDetails user, ChatRoomReq request) {
        // 우선 게시글 기준으로 전체 채팅방 조회
        List<ChatRoom> existingRooms = chatRoomRepository.findByTypeAndTypeId(request.getType(), request.getTypeId());
        // 그 중, 참여자가 동일한 방이 있는지 체크
        Optional<ChatRoom> existing = existingRooms.stream()
                .filter(room -> {
                    List<Long> participantIds = room.getParticipants().stream()
                            .map(p -> p.getUser().getId())
                            .toList();
                    // 두 명의 유저가 모두 포함되어 있는지 확인
                    return participantIds.contains(request.getFromUserId()) && participantIds.contains(request.getToUserId());
                })
                .findFirst();
        log.info("Creating chat room {}", existing);
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 유저 계정을 찾을 수 없습니다."));
        log.info("user {}", toUser);
        if (existing.isPresent()) {
            // 이미 존재하면 있는 방을 보내줘야됨
            ChatRoom chatRoom = existing.get();
            return getEnterChatRoom(user,chatRoom.getId());
        }else {
            //  게시글 존재 여부 및 상대방 유저 조회
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

            // 자신이 자기 게시글에 채팅 거는 경우 방지
            if (targetUser.equals(user.getUser())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인에게는 채팅을 보낼 수 없습니다.");
            }

            ChatRoom newChatRoom = ChatRoom.builder()
                    .type(request.getType())
                    .typeId(request.getTypeId())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .status(ChatRoom.RoomStatus.ACTIVE)
                    .build();

            chatRoomRepository.save(newChatRoom);

            if (request.getMessageType().equals(ChatMessages.messageType.text)) {
                ChatMessages newChatMessages = ChatMessages.builder()
                        .chatRoom(newChatRoom)
                        .chatType(request.getMessageType())
                        .message(request.getMessage())
                        .sender(user.getUser())
                        .createdAt(LocalDateTime.now())
                        .build();
                chatMessagesRepository.save(newChatMessages);
            }
//       else if(request.getChatType().equals(ChatMessages.ChatType.이미지)) {
//            ChatMessages newChatMessages = ChatMessages.builder()
//                    .chatId(newChatRoom.getId())
//                    .chatType(ChatMessages.ChatType.이미지)
//                    .message("여기에 이미지 url을 넣어야됨!!!!!!! 일단 이미지 없어서 블락 처리함")
//                    .senderId(user.getUser().getId())
//                    .createdAt(LocalDateTime.now())
//                    .build();
//            chatMessagesRepository.save(newChatMessages);
//        }
            ChatStatus myStatus = ChatStatus.builder()
                    .chatRoom(newChatRoom)
                    .user(user.getUser())
                    .firstDate(LocalDateTime.now())
                    .lastDate(LocalDateTime.now())
                    .chatStatus(true)
                    .build();

            ChatStatus opponentStatus = ChatStatus.builder()
                    .chatRoom(newChatRoom)
                    .user(toUser)
                    .firstDate(LocalDateTime.now())
                    .lastDate(LocalDateTime.now())
                    .chatStatus(true)
                    .build();
            chatStatusRepository.saveAll(List.of(myStatus, opponentStatus));
            return getEnterChatRoom(user,newChatRoom.getId());
        }
    }

    public ChatEnterRes getEnterChatRoom(CustomUserDetails user, Long roomId) {
        // 1. 채팅방 조회`
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 해당 방의 참여자 조회
        List<ChatStatus> participants = chatStatusRepository.findByChatRoomId(roomId);

        // 상대방 찾기
        User opponent = participants.stream()
                .map(ChatStatus::getUser)
                .filter(u -> !u.getId().equals(user.getUser().getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "상대방을 찾을 수 없습니다."));

        // 메시지 목록 (최근순 정렬)
        List<ChatMessages> messageList = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        // 마지막 접속 시간 업데이트
        chatStatusRepository.findByChatRoomIdAndUserId(roomId, user.getUser().getId())
                .ifPresent(status -> {
                    status.setLastDate(LocalDateTime.now());
                    chatStatusRepository.save(status);
                });

        Object extraInfo = null;
        switch (room.getType()) {
            case LOST_ITEM -> extraInfo = lostItemRepository.findById(room.getTypeId()).orElse(null);
            case USED_ITEM -> extraInfo = usedItemRepository.findById(room.getTypeId()).orElse(null);
            case GROUP_BUY -> extraInfo = groupBuyRepository.findById(room.getTypeId()).orElse(null);
            case ADMIN -> {
                // admin 계정 정보 직접 조회
                User adminUser = userRepository.findByEmail(adminConfig.getEmail())
                        .orElse(null); // admin 계정 이메일에 맞게 수정
                extraInfo = adminUser != null ? adminUser.getText() : "**채팅 공지사항**";
            }
        }
        // DTO로 반환
        return ChatEnterRes.from(room, opponent, messageList, extraInfo);
    }


//    public List<ChatMessagesRes> getEnterChatRoomt(CustomUserDetails user, Long roomId) {
//        // 최신 메시지를 가져오기 위해 Page 0, Size 20, createdAt 내림차순 정렬을 사용합니다.
//
//        PageRequest pageRequest = PageRequest.of(0, MESSAGE_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
//
//        Slice<ChatMessages> slice = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageRequest);
//
//
//        // 결과는 최신순(DESC)으로 가져왔으므로, 클라이언트에게 보여줄 때는 오름차순(ASC)으로 다시 정렬하여 반환합니다.
//        // 채팅은 오래된 메시지가 위에 있고, 최신 메시지가 가장 아래에 위치하는 것이 일반적입니다.
//        return slice.getContent().stream()
//                .map(this::toDto)
//                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt())) // ASC로 재정렬
//                .collect(Collectors.toList());
//    }
    // 20단위 쪼개기
//    public Slice<ChatMessagesRes> getPreviousChatMessages(CustomUserDetails user, Long roomId, int pageNumber) {
//        // 클라이언트에서 pageNumber를 '1'로 보내면, 서비스에서는 '1' 페이지(즉, 두 번째 20개 묶음)를 가져옵니다.
//        // 정렬은 최신순(DESC)을 유지합니다.
//        PageRequest pageRequest = PageRequest.of(pageNumber, MESSAGE_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
//
//        Slice<ChatMessages> slice = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageRequest);
//
//        // Slice의 데이터를 DTO로 변환합니다.
//        List<ChatMessagesRes> dtoList = slice.getContent().stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//
//        // Slice는 content 리스트와 hasNext 같은 페이징 정보를 모두 포함합니다.
//        return slice.map(this::toDto); // Slice.map()을 사용하여 편리하게 DTO로 변환
//    }

    private ChatMessagesRes toDto(ChatMessages chatMessage) {
        return ChatMessagesRes.builder()
                .chatType(chatMessage.getChatType())
                .message(chatMessage.getMessage())
                .senderId(chatMessage.getSender().getId())

                .createdAt(chatMessage.getCreatedAt())
                .build();
    }

    public void deleteChatRoom(CustomUserDetails user, Long chatRoomId) {
        ChatStatus chatStatus = chatStatusRepository.findByUserIdAndChatRoomId(
                user.getUser().getId(), chatRoomId);
        if (chatStatus == null) {
            throw new IllegalArgumentException("해당 채팅방 상태를 찾을 수 없습니다.");
        }
        chatStatus.setChatStatus(false);
        chatStatusRepository.save(chatStatus);
    }
    /// ///////////////// -------------------------------------------해당 코드는 채팅 방이 만들어진후 태스트가 필요함
    @Transactional
    public void updateTradeStatus(CustomUserDetails user, Long roomId, Enum.UsedItemStatus newStatus) {
        User currentUser = user.getUser();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다. ID: " + roomId));
        // 2. 이 채팅방이 '중고거래'에서 생성된 것인지 확인.
        if (chatRoom.getType() != Enum.ChatType.USED_ITEM) {
            throw new IllegalArgumentException("거래 상태를 변경할 수 없는 종류의 채팅방입니다.");
        }
        // 3. 채팅방에 연결된 중고거래 게시글을 찾습니다.
        Long usedItemId = chatRoom.getTypeId();
        UsedItem usedItem = usedItemRepository.findById(usedItemId)
                .orElseThrow(() -> new ResourceNotFoundException("연결된 중고거래 게시글을 찾을 수 없습니다. ID: " + usedItemId));

        // 4. 요청한 사용자가 게시글의 판매자인지 확인하여 권한을 검증
        if (!usedItem.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("거래 상태를 변경할 권한이 없습니다.");
        }
        // 5. 모든 검증이 통과되면 게시글의 상태를 변경합니다.
        usedItem.setStatus(newStatus);

        // 6. (선택사항) 채팅방의 모든 구독자에게 상태 변경 알림 메시지를 보냅니다.
        String notificationMessage = "판매자가 상품 상태를 '" + newStatus + "'(으)로 변경했습니다.";

        // 시스템 알림을 위한 DTO를 생성하여 전송
        Notificationres notification = Notificationres.builder()
                .chatType(Enum.ChatType.Chat)
                .typeId(chatRoom.getId())
                .message(notificationMessage)
                .build();
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, notification);
    }

    public void saveMessage(CustomUserDetails user, ChatMessageRequest message) {
        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."
                ));
        ChatMessages newChatMessages = ChatMessages.builder()
                .chatRoom(chatRoom)
                .chatType(message.getType())
                .message(message.getMessage())
                .sender(user.getUser())
                .createdAt(LocalDateTime.now())
                .build();
        chatMessagesRepository.save(newChatMessages);
    }

//
//    public ChatMessages saveMessage(CustomUserDetails user, Long chatRoomId, ChatMessageRequest message) {
//        return null;
//    }
}
