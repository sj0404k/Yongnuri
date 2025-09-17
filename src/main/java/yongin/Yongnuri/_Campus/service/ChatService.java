package yongin.Yongnuri._Campus.servise;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.ChatMessages;
import yongin.Yongnuri._Campus.domain.ChatRoom;
import yongin.Yongnuri._Campus.domain.ChatStatus;
import yongin.Yongnuri._Campus.dto.ChatRoomDto;
import yongin.Yongnuri._Campus.dto.ChatRoomReq;
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

    public List<ChatRoomDto> getChatRooms(CustomUserDetails user, String type) {

        ChatRoom.ChatType chatType;
        try {
            chatType = ChatRoom.ChatType.valueOf(type);
        } catch (IllegalArgumentException e) {
            chatType = ChatRoom.ChatType.전체; // 잘못된 값이면 전체로 처리
        }

        List<ChatRoom> rooms;
        if (chatType == ChatRoom.ChatType.전체) {
            rooms = chatRoomRepository.findByFromUserIdOrToUserId(user.getUser().getId(), user.getUser().getId());
        } else {
            rooms = chatRoomRepository.findByFromUserIdOrToUserIdAndType(user.getUser().getId(), user.getUser().getId(), chatType);
        }
        List<ChatRoomDto> res = rooms.stream()
                .map(ChatRoomDto::fromEntity)
                .collect(Collectors.toList());
        return res;
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

        if(request.getChatType().equals(ChatMessages.ChatType.텍스트)) {
            ChatMessages newChatMessages = ChatMessages.builder()
                    .chatId(newChatRoom.getId())
                    .chatType(ChatMessages.ChatType.텍스트)
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
//
//    public ChatMessages saveMessage(CustomUserDetails user, Long chatRoomId, ChatMessageRequest message) {
//        return null;
//    }
}
