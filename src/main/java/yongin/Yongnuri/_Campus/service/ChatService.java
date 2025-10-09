package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.ChatMessages;
import yongin.Yongnuri._Campus.domain.ChatRoom;
import yongin.Yongnuri._Campus.domain.ChatStatus;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.dto.chat.ChatMessagesRes;
import yongin.Yongnuri._Campus.dto.chat.ChatRoomDto;
import yongin.Yongnuri._Campus.dto.chat.ChatRoomReq;
import yongin.Yongnuri._Campus.repository.ChatMessagesRepository;
import yongin.Yongnuri._Campus.repository.ChatRoomRepository;
import yongin.Yongnuri._Campus.repository.ChatStatusRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatStatusRepository chatStatusRepository;
    private static final int MESSAGE_PAGE_SIZE = 20;// 채팅 20개 씩 보여주기

    public List<ChatRoomDto> getChatRooms(CustomUserDetails user, Enum.ChatType type) {

       Enum.ChatType chatType;
        try {
            chatType = type;
        } catch (IllegalArgumentException e) {
            chatType = Enum.ChatType.ALL; // 잘못된 값이면 전체로 처리
        }

        List<ChatRoom> rooms;
        if (chatType == Enum.ChatType.ALL) {
            rooms = chatRoomRepository.findByFromUserIdOrToUserId(user.getUser().getId(), user.getUser().getId());
        } else {
            rooms = chatRoomRepository.findByFromUserIdOrToUserIdAndType(user.getUser().getId(), user.getUser().getId(), chatType);
        }
        return rooms.stream()
                .map(ChatRoomDto::fromEntity)
                .collect(Collectors.toList());
    }

//    public ChatRoomDto getChatRoom(CustomUserDetails user, Long chatRoomId) {
//
//    }

    public void createChatRoom(CustomUserDetails user, ChatRoomReq request) {
        // 1. 이미 같은 조건의 채팅방이 있는지 중복 확인
        Optional<ChatRoom> existing = chatRoomRepository.findByTypeAndTypeIdAndFromUserIdAndToUserId(
                request.getType(),
                request.getTypeId(),
                user.getUser().getId(),
                request.getFromUserId()
        );

        if (existing.isPresent()) {
            throw new IllegalStateException("이미 존재하는 채팅방입니다.");
        }
        ChatRoom newChatRoom = ChatRoom.builder()
                .type(request.getType())
                .typeId(request.getTypeId())
                .fromUserId(user.getUser().getId()) // 현재 로그인한 사용자 ID가 채팅방을 시작한 사용자
                .toUserId(request.getFromUserId()) // request의 fromUserId가 상대방 (채팅 대상) 사용자
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .lastMessage(request.getMessage())
//                .status(1)
                .build();

        chatRoomRepository.save(newChatRoom);

        if(request.getMessageType().equals(ChatMessages.messageType.텍스트)) {
            ChatMessages newChatMessages = ChatMessages.builder()
                    .chatRoomId(newChatRoom.getId())
                    .chatType(ChatMessages.messageType.텍스트)
                    .message(request.getMessage())
                    .senderId(user.getUser().getId())
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
        ChatStatus newchatStatus = ChatStatus.builder()
                .chatRoomId(newChatRoom.getId())
                .userId(user.getUser().getId())
                .lastDate(LocalDateTime.now())
                .chatStatus(true)
                .build();
        chatStatusRepository.save(newchatStatus);
        return ;
    }

    public List<ChatMessagesRes> getEnterChatRoom(CustomUserDetails user, Long roomId) {
        // 1. 채팅방 가져오기
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("해당 채팅방이 존재하지 않습니다."));

        // 2. 나의 채팅방 상태 가져오기
        ChatStatus myChatStatus = chatStatusRepository.findByUserIdAndChatRoomId(user.getUser().getId(), roomId);

        // 3. 마지막 접속시간 업데이트
        myChatStatus.setLastDate(LocalDateTime.now());
        chatStatusRepository.save(myChatStatus);

        // 4. 상대방 ID 구하기
        Long fromUserId;
        if (user.getUser().getId().equals(chatRoom.getFromUserId())) {
            fromUserId = chatRoom.getToUserId();
        } else {
            fromUserId = chatRoom.getFromUserId();
        }

        // 5. 상대방의 상태 가져오기 (마지막 접속 시간)
        ChatStatus otherChatStatus = chatStatusRepository.findByUserIdAndChatRoomId(fromUserId, roomId);
        LocalDateTime otherLastDate = otherChatStatus != null ? otherChatStatus.getLastDate() : LocalDateTime.MIN;

        // 6. 채팅 메시지 전체 불러오기
        List<ChatMessages> messages = chatMessagesRepository.findByChatRoomId(roomId);

        // 7. 메시지를 DTO로 변환 + 상대방 마지막 접속 이후의 메시지 개수를 count로 넣기
        return messages.stream()
                .map(msg -> {
                    int count = 0;
                    if (msg.getCreatedAt().isAfter(otherLastDate)) {
                        // 상대방이 못 본 메시지라면 count 증가
                        count = 1;
                    }

                    return ChatMessagesRes.builder()
                            .chatType(msg.getChatType())
                            .message(msg.getMessage())
                            .senderId(msg.getSenderId())
                            .createdAt(msg.getCreatedAt())
                            .count(count)
                            .build();
                })
                .toList();
    }


    public List<ChatMessagesRes> getEnterChatRoomt(CustomUserDetails user, Long roomId) {
        // 최신 메시지를 가져오기 위해 Page 0, Size 20, createdAt 내림차순 정렬을 사용합니다.

        PageRequest pageRequest = PageRequest.of(0, MESSAGE_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<ChatMessages> slice = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageRequest);


        // 결과는 최신순(DESC)으로 가져왔으므로, 클라이언트에게 보여줄 때는 오름차순(ASC)으로 다시 정렬하여 반환합니다.
        // 채팅은 오래된 메시지가 위에 있고, 최신 메시지가 가장 아래에 위치하는 것이 일반적입니다.
        return slice.getContent().stream()
                .map(this::toDto)
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt())) // ASC로 재정렬
                .collect(Collectors.toList());
    }
    // 20단위 쪼개기
    public Slice<ChatMessagesRes> getPreviousChatMessages(CustomUserDetails user, Long roomId, int pageNumber) {
        // 클라이언트에서 pageNumber를 '1'로 보내면, 서비스에서는 '1' 페이지(즉, 두 번째 20개 묶음)를 가져옵니다.
        // 정렬은 최신순(DESC)을 유지합니다.
        PageRequest pageRequest = PageRequest.of(pageNumber, MESSAGE_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<ChatMessages> slice = chatMessagesRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageRequest);

        // Slice의 데이터를 DTO로 변환합니다.
        List<ChatMessagesRes> dtoList = slice.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // Slice는 content 리스트와 hasNext 같은 페이징 정보를 모두 포함합니다.
        return slice.map(this::toDto); // Slice.map()을 사용하여 편리하게 DTO로 변환
    }

    private ChatMessagesRes toDto(ChatMessages chatMessage) {
        return ChatMessagesRes.builder()
                .chatType(chatMessage.getChatType())
                .message(chatMessage.getMessage())
                .senderId(chatMessage.getSenderId())

                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
//
//    public ChatMessages saveMessage(CustomUserDetails user, Long chatRoomId, ChatMessageRequest message) {
//        return null;
//    }
}
