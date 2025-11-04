package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /** ✅ 채팅방 목록 — 마지막 메시지 기준 최신순 정렬 */
    @Transactional(readOnly = false)
    public List<ChatRoomDto> getChatRooms(CustomUserDetails user, Enum.ChatType type) {
        log.debug("getChatRooms({}, {})", user.getUser().getId(), type);
        // 1️⃣ 내가 삭제하지 않은 참여방만 조회
        List<ChatStatus> activeStatuses = chatStatusRepository.findByUserIdAndChatStatusTrue(user.getUser().getId());
        if (activeStatuses.isEmpty()) return Collections.emptyList();

        List<Long> activeRoomIds = activeStatuses.stream()
                .map(cs -> cs.getChatRoom().getId())
                .toList();

        // 2️⃣ 타입별 필터
        Enum.ChatType chatType = (type != null) ? type : Enum.ChatType.ALL;
        List<ChatRoom> rooms = (chatType == Enum.ChatType.ALL)
                ? chatRoomRepository.findByIdInWithParticipants(activeRoomIds)
                : chatRoomRepository.findByIdInAndTypeWithParticipants(activeRoomIds, chatType);

        if (rooms.isEmpty()) return Collections.emptyList();

        // 3️⃣ 각 방의 "마지막 메시지" 한 번에 조회
        Map<Long, ChatMessages> lastMessagesMap = chatMessagesRepository.findLastMessagesByRoomIds(activeRoomIds)
                .stream()
                .collect(Collectors.toMap(msg -> msg.getChatRoom().getId(), Function.identity()));

        // 4️⃣ DTO + 정렬 기준 시각 계산
        List<WithSort<ChatRoomDto>> boxed = new ArrayList<>(rooms.size());
        for (ChatRoom room : rooms) {
            User opponentUser = room.getParticipants().stream()
                    .map(ChatStatus::getUser)
                    .filter(u -> !u.getId().equals(user.getUser().getId()))
                    .findFirst()
                    .orElse(null);

            ChatMessages lastMessage = lastMessagesMap.get(room.getId());

            // ✅ 정렬 기준: 마지막 메시지 시각(우선) → 없으면 room.updateTime
            LocalDateTime sortTs = (lastMessage != null && lastMessage.getCreatedAt() != null)
                    ? lastMessage.getCreatedAt()
                    : room.getUpdateTime();

            ChatRoomDto dto = ChatRoomDto.fromEntity(room, opponentUser, lastMessage);
            boxed.add(new WithSort<>(dto, sortTs != null ? sortTs : LocalDateTime.MIN));
        }

        // 5️⃣ 최신순(내림차순)
        boxed.sort((a, b) -> b.sortKey.compareTo(a.sortKey));
        return boxed.stream().map(w -> w.value).toList();
    }

    private static class WithSort<T> {
        final T value;
        final LocalDateTime sortKey;
        WithSort(T v, LocalDateTime k) { this.value = v; this.sortKey = k; }
    }

    /** 채팅방 생성 */
    @Transactional
    public ChatEnterRes createChatRoom(CustomUserDetails user, ChatRoomReq request) {
        log.info("createChatRoom({}, {})", user.getUser().getId(), request);

        // 🔹 ADMIN 채팅일 경우 typeId 없이 처리
        if (Enum.ChatType.ADMIN.equals(request.getType())) {
            log.info("ADMIN 타입 채팅 생성 요청입니다.");

            // 이미 ADMIN 채팅방이 존재하는지 확인 (한 명당 하나만 허용할 경우)
            Optional<ChatRoom> existingAdminRoom = chatRoomRepository.findByTypeAndParticipantsUserId(Enum.ChatType.ADMIN, user.getUser().getId());
            if (existingAdminRoom.isPresent()) {
                log.info("기존 ADMIN 채팅방 존재: {}", existingAdminRoom.get().getId());
                return getEnterChatRoom(user, existingAdminRoom.get().getId());
            }

            // 🔹 새 ADMIN 방 생성
            ChatRoom adminRoom = ChatRoom.builder()
                    .type(Enum.ChatType.ADMIN)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .status(ChatRoom.RoomStatus.ACTIVE)
                    .build();
            chatRoomRepository.save(adminRoom);

            // 🔹 관리자(User) 조회 — 예시로 관리자 이메일 기준
            User adminUser = userRepository.findByEmail("admin@yongin.ac.kr")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "관리자 계정을 찾을 수 없습니다."));

            // 🔹 채팅 상태 등록
            ChatStatus userStatus = ChatStatus.builder()
                    .chatRoom(adminRoom)
                    .user(user.getUser())
                    .firstDate(LocalDateTime.now())
                    .lastDate(LocalDateTime.now())
                    .chatStatus(true)
                    .build();

            ChatStatus adminStatus = ChatStatus.builder()
                    .chatRoom(adminRoom)
                    .user(adminUser)
                    .firstDate(LocalDateTime.now())
                    .lastDate(LocalDateTime.now())
                    .chatStatus(true)
                    .build();

            chatStatusRepository.saveAll(List.of(userStatus, adminStatus));

            log.info("ADMIN 채팅방 생성 완료. roomId={}", adminRoom.getId());
            return getEnterChatRoom(user, adminRoom.getId());
        }

        // 🔹 일반 채팅 로직 (기존 코드 그대로)
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

            ChatRoom newChatRoom = ChatRoom.builder()
                    .type(request.getType())
                    .typeId(request.getTypeId())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .status(ChatRoom.RoomStatus.ACTIVE)
                    .build();
            chatRoomRepository.save(newChatRoom);

            if (request.getMessage() != null) {
                ChatMessages initMsg = ChatMessages.builder()
                        .chatRoom(newChatRoom)
                        .chatType(request.getMessageType())
                        .message(request.getMessage())
                        .sender(user.getUser())
                        .createdAt(LocalDateTime.now())
                        .build();
                chatMessagesRepository.save(initMsg);

                newChatRoom.setUpdateTime(initMsg.getCreatedAt());
                chatRoomRepository.saveAndFlush(newChatRoom);
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

    /** 채팅방 입장 */
    @Transactional(readOnly = true)
    public ChatEnterRes getEnterChatRoom(CustomUserDetails user, Long roomId) {
        log.info("getEnterChatRoom({}, {})", user.getUser().getId(), roomId);
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
        ChatStatus myStatus = participants.stream()
                .filter(p -> p.getUser().getId().equals(user.getUser().getId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("이 채팅방에 접근할 권한이 없습니다."));
        List<ChatMessages> messageList = chatMessagesRepository.findMessagesAfterDeletedAt(roomId, myStatus.getDeletedAt());
//        List<ChatMessages> messageList = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        Object extraInfo = null;
        String thumbnailUrl = null;
        switch (room.getType()) {
            case LOST_ITEM -> {
                LostItem lost = lostItemRepository.findById(room.getTypeId()).orElse(null);
                extraInfo = lost;
                if (lost != null && Boolean.TRUE.equals(lost.getIsImages())) {
                    thumbnailUrl = imageRepository.findByTypeAndTypeIdInAndSequence("LOST_ITEM", List.of(lost.getId()), 1)
                            .stream().findFirst().map(Image::getImageUrl).orElse(null);
                }
            }
            case USED_ITEM -> {
                UsedItem used = usedItemRepository.findById(room.getTypeId()).orElse(null);
                extraInfo = used;
                if (used != null && Boolean.TRUE.equals(used.getIsImages())) {
                    thumbnailUrl = imageRepository.findByTypeAndTypeIdInAndSequence("USED_ITEM", List.of(used.getId()), 1)
                            .stream().findFirst().map(Image::getImageUrl).orElse(null);
                }
            }
            case GROUP_BUY -> {
                GroupBuy group = groupBuyRepository.findById(room.getTypeId()).orElse(null);
                extraInfo = group;
                if (group != null && Boolean.TRUE.equals(group.getIsImages())) {
                    thumbnailUrl = imageRepository.findByTypeAndTypeIdInAndSequence("GROUP_BUY", List.of(group.getId()), 1)
                            .stream().findFirst().map(Image::getImageUrl).orElse(null);
                }
            }
            case ADMIN -> {
                User adminUser = userRepository.findByEmail(adminConfig.getEmail()).orElse(null);
                extraInfo = (adminUser != null && adminUser.getText() != null)
                        ? adminUser.getText()
                        : "**채팅 공지사항**";
            }
        }
        return ChatEnterRes.from(room, opponent, messageList, extraInfo, thumbnailUrl);
    }

    /** 읽음 시각 갱신 */
    @Transactional
    public void markRead(CustomUserDetails user, Long roomId) {
        log.info("markRead({}, {})", user.getUser().getId(), roomId);
        int updated = chatStatusRepository.touchLastDate(roomId, user.getUser().getId(), LocalDateTime.now());
        if (updated == 0) throw new AccessDeniedException("이 채팅방에 접근할 권한이 없습니다.");
    }

    /** 내 목록에서 채팅방 삭제 (상대방 유지) */
    @Transactional
    public void deleteChatRoom(CustomUserDetails user, Long chatRoomId) {
        log.info("deleteChatRoom({}, {})", user.getUser().getId(), chatRoomId);
        ChatStatus chatStatus = chatStatusRepository.findByUserIdAndChatRoomId(user.getUser().getId(), chatRoomId);
        if (chatStatus == null)
            throw new IllegalArgumentException("해당 채팅방에 대한 참여 정보를 찾을 수 없습니다.");
        chatStatus.setChatStatus(false);
        chatStatus.setDeletedAt(LocalDateTime.now());
        chatStatusRepository.save(chatStatus);
    }

    /** 거래 상태 변경 */
    @Transactional
    public void updateTradeStatus(CustomUserDetails user, Long roomId, Enum.UsedItemStatus newStatus) {
        log.info("updateTradeStatus({}, {})", user.getUser().getId(), roomId);
        User currentUser = user.getUser();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        if (chatRoom.getType() != Enum.ChatType.USED_ITEM)
            throw new IllegalArgumentException("거래 상태를 변경할 수 없는 종류의 채팅방입니다.");

        Long usedItemId = chatRoom.getTypeId();
        UsedItem usedItem = usedItemRepository.findById(usedItemId)
                .orElseThrow(() -> new ResourceNotFoundException("연결된 중고거래 게시글을 찾을 수 없습니다."));

        if (!usedItem.getUserId().equals(currentUser.getId()))
            throw new AccessDeniedException("거래 상태를 변경할 권한이 없습니다.");

        usedItem.setStatus(newStatus);
        usedItemRepository.save(usedItem);

        String msg = "판매자가 상품 상태를 '" + newStatus + "'(으)로 변경했습니다.";
        Notificationres notification = Notificationres.builder()
                .chatType(Enum.ChatType.Chat)
                .typeId(chatRoom.getId())
                .message(msg)
                .build();
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, notification);
    }

    /** ✅ 메시지 저장 — 마지막 메시지 시간으로 updateTime 갱신 */
    @Transactional
    public ChatMessagesRes saveMessage(CustomUserDetails user, ChatMessageRequest message) {
        log.info("saveMessage({}, {})", user.getUser().getId(), message);
        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("메시지를 보낼 채팅방을 찾을 수 없습니다."));

        ChatMessages newMsg = ChatMessages.builder()
                .chatRoom(chatRoom)
                .chatType(message.getType())
                .message(message.getMessage())
                .sender(user.getUser())
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessages saved = chatMessagesRepository.save(newMsg);

        // ✅ 핵심: 방의 updateTime을 최신 메시지로 갱신하고 즉시 flush
        chatRoom.setUpdateTime(saved.getCreatedAt());
        chatRoomRepository.saveAndFlush(chatRoom);

        log.info(">>> ChatRoom {} updateTime 갱신 = {}", chatRoom.getId(), saved.getCreatedAt());

        // 상대방 상태 확인 및 자동 복구
        List<ChatStatus> statuses = chatStatusRepository.findByChatRoomId(chatRoom.getId());
        for (ChatStatus status : statuses) {
            // 상대방(메시지 보낸 사람 제외)
            if (!status.getUser().getId().equals(user.getUser().getId())) {
                if (!status.isChatStatus()) {
                    // ✅ 삭제한 상대방 복구
                    status.setChatStatus(true);
//                    status.setDeletedAt(null);
                    status.setLastDate(LocalDateTime.now());
                    chatStatusRepository.save(status);
                    log.info(">>> 복구: {}님이 삭제했던 방 {} 다시 활성화됨", status.getUser().getEmail(), chatRoom.getId());
                }
            }
        }
        return ChatMessagesRes.builder()
                .chatType(saved.getChatType())
                .message(saved.getMessage())
                .senderId(saved.getSender() != null ? saved.getSender().getId() : null)
                .senderEmail(saved.getSender() != null ? saved.getSender().getEmail().toLowerCase() : null)
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
