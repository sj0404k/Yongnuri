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
import java.util.*;
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

    /** 채팅방 목록 */
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRooms(CustomUserDetails user, Enum.ChatType type) {
        List<ChatStatus> activeStatuses = chatStatusRepository.findByUserIdAndChatStatusTrue(user.getUser().getId());
        if (activeStatuses.isEmpty()) return Collections.emptyList();

        List<Long> activeRoomIds = activeStatuses.stream()
                .map(cs -> cs.getChatRoom().getId())
                .toList();

        Enum.ChatType chatType = (type != null) ? type : Enum.ChatType.ALL;
        List<ChatRoom> rooms = (chatType == Enum.ChatType.ALL)
                ? chatRoomRepository.findByIdInWithParticipants(activeRoomIds)
                : chatRoomRepository.findByIdInAndTypeWithParticipants(activeRoomIds, chatType);

        Map<Long, ChatMessages> lastMessagesMap = chatMessagesRepository.findLastMessagesByRoomIds(activeRoomIds)
                .stream()
                .collect(Collectors.toMap(msg -> msg.getChatRoom().getId(), Function.identity()));

        return rooms.stream()
                .map(room -> {
                    User opponentUser = room.getParticipants().stream()
                            .map(ChatStatus::getUser)
                            .filter(u -> !u.getId().equals(user.getUser().getId()))
                            .findFirst()
                            .orElse(null);
                    ChatMessages lastMessage = lastMessagesMap.get(room.getId());
                    return ChatRoomDto.fromEntity(room, opponentUser, lastMessage);
                })
                .collect(Collectors.toList());
    }

    /** 채팅방 생성 */
    @Transactional
    public ChatEnterRes createChatRoom(CustomUserDetails user, ChatRoomReq request) {

        List<ChatRoom> existingRooms = chatRoomRepository.findByTypeAndTypeIdWithParticipantsAndLock(
                request.getType(), request.getTypeId());

        Optional<ChatRoom> existing = existingRooms.stream()
                .filter(room -> {
                    List<Long> participantIds = room.getParticipants().stream()
                            .map(p -> p.getUser().getId())
                            .toList();
                    return participantIds.contains(user.getUser().getId())
                            && participantIds.contains(request.getToUserId());
                })
                .findFirst();

        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅 상대를 찾을 수 없습니다."));

        if (existing.isPresent()) {
            log.info("Existing room found {}. Entering.", existing.get().getId());
            return getEnterChatRoom(user, existing.get().getId());
        } else {
            log.info("No existing room. Creating new one for post {} with user {}",
                    request.getTypeId(), toUser.getId());

            if (toUser.getId().equals(user.getUser().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인에게는 채팅을 보낼 수 없습니다.");
            }

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
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시글 작성자를 찾을 수 없습니다."));
                    }
                    case GROUP_BUY -> {
                        GroupBuy groupBuy = groupBuyRepository.findById(request.getTypeId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공동구매 게시글을 찾을 수 없습니다."));
                        targetUser = userRepository.findById(groupBuy.getUserId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시글 작성자를 찾을 수 없습니다."));
                    }
                    case ADMIN -> targetUser = userRepository.findByEmail(adminConfig.getEmail())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관리자 계정을 찾을 수 없습니다."));
                    default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 채팅 타입입니다.");
                }
                if (!targetUser.getId().equals(toUser.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시글 작성자가 아닌 사용자에게 채팅을 요청했습니다.");
                }
            }

            ChatRoom newChatRoom = ChatRoom.builder()
                    .type(request.getType())
                    .typeId(request.getTypeId())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .status(ChatRoom.RoomStatus.ACTIVE)
                    .build();
            chatRoomRepository.save(newChatRoom);

            // ✅ 초기 메시지 저장 (sender 반드시 세팅)
            if (request.getMessageType() == ChatMessages.messageType.text && request.getMessage() != null) {
                ChatMessages newChatMessages = ChatMessages.builder()
                        .chatRoom(newChatRoom)
                        .chatType(ChatMessages.messageType.text)
                        .message(request.getMessage())
                        .sender(user.getUser())               // ✅ 중요
                        .createdAt(LocalDateTime.now())
                        .build();
                chatMessagesRepository.save(newChatMessages);
            }// 초기 메시지가 이미지인 경우
            else if (request.getMessageType() == ChatMessages.messageType.img && request.getMessage() != null) {
            ChatMessages newChatMessages = ChatMessages.builder()
                    .chatRoom(newChatRoom)
                    .chatType(ChatMessages.messageType.img)
                    .message(request.getMessage())
                    .sender(user.getUser())
                    .createdAt(LocalDateTime.now())
                    .build();
            chatMessagesRepository.save(newChatMessages);
        }

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
            return getEnterChatRoom(user, newChatRoom.getId());
        }
    }

    /** 채팅방 입장 상세 — ✅ 항상 senderId/senderEmail 포함해서 반환 */
    @Transactional(readOnly = true)
    public ChatEnterRes getEnterChatRoom(CustomUserDetails user, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        List<ChatStatus> participants = chatStatusRepository.findByChatRoomId(roomId);

        participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getUser().getId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("이 채팅방에 접근할 권한이 없습니다."));

        User opponent = participants.stream()
                .map(ChatStatus::getUser)
                .filter(u -> !u.getId().equals(user.getUser().getId()))
                .findFirst()
                .orElse(null);

        List<ChatMessages> messageList = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        // 7. 게시글 등 추가 정보 조회
        Object extraInfo = null;
        String thumbnailUrl = null;
        switch (room.getType()) {
            case LOST_ITEM: {
                LostItem lost = lostItemRepository.findById(room.getTypeId()).orElse(null);
                extraInfo = lost;

                if (lost != null && Boolean.TRUE.equals(lost.getIsImages())) {
                    thumbnailUrl = imageRepository
                            .findByTypeAndTypeIdInAndSequence("LOST_ITEM", List.of(lost.getId()), 1)
                            .stream()
                            .findFirst()
                            .map(Image::getImageUrl)
                            .orElse(null);
                }
                break;
            }
            case USED_ITEM: {
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
                break;
            }
            case GROUP_BUY: {
                GroupBuy group = groupBuyRepository.findById(room.getTypeId()).orElse(null);
                extraInfo = group;

                if (group != null && Boolean.TRUE.equals(group.getIsImages())) {
                    thumbnailUrl = imageRepository
                            .findByTypeAndTypeIdInAndSequence("GROUP_BUY", List.of(group.getId()), 1)
                            .stream()
                            .findFirst()
                            .map(Image::getImageUrl)
                            .orElse(null);
                }
                break;
            }
            case ADMIN: {
                User adminUser = userRepository.findByEmail(adminConfig.getEmail())
                        .orElse(null);
                extraInfo = (adminUser != null && adminUser.getText() != null)
                        ? adminUser.getText()
                        : "**채팅 공지사항**";
                break;
            }
        }

        // 8. DTO로 반환
        return ChatEnterRes.from(room, opponent, messageList, extraInfo, thumbnailUrl);
    }


    /** (신규) 읽음/입장 시각 갱신 — 단건 UPDATE */
    @Transactional
    public void markRead(CustomUserDetails user, Long roomId) {
        int updated = chatStatusRepository.touchLastDate(roomId, user.getUser().getId(), LocalDateTime.now());
        if (updated == 0) {
            throw new AccessDeniedException("이 채팅방에 접근할 권한이 없습니다.");
        }
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
        usedItemRepository.save(usedItem);

        String notificationMessage = "판매자가 상품 상태를 '" + newStatus + "'(으)로 변경했습니다.";
        Notificationres notification = Notificationres.builder()
                .chatType(Enum.ChatType.Chat)
                .typeId(chatRoom.getId())
                .message(notificationMessage)
                .build();
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, notification);
    }

    /** 메시지 저장(웹소켓/HTTP 공용) — ✅ sender 보장 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChatMessagesRes saveMessage(CustomUserDetails user, ChatMessageRequest message) {
        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("메시지를 보낼 채팅방을 찾을 수 없습니다."));

        ChatMessages newChatMessages = ChatMessages.builder()
                .chatRoom(chatRoom)
                .chatType(message.getType())
                .message(message.getMessage())
                .sender(user.getUser())                // ✅ 중요: 항상 설정
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessages saved = chatMessagesRepository.save(newChatMessages);

        return ChatMessagesRes.builder()
                .chatType(saved.getChatType())
                .message(saved.getMessage())
                .senderId(saved.getSender() != null ? saved.getSender().getId() : null)
                .senderEmail(saved.getSender() != null ? saved.getSender().getEmail().toLowerCase() : null)
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
