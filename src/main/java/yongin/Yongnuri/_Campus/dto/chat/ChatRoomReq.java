package yongin.Yongnuri._Campus.dto.chat;

import lombok.Getter;
import yongin.Yongnuri._Campus.domain.ChatMessages;
import yongin.Yongnuri._Campus.domain.ChatRoom;
import yongin.Yongnuri._Campus.domain.Enum;

@Getter

public class ChatRoomReq {
    private Enum.ChatType type;
    private Long typeId;;
    private Long userId;
    private Long fromUserId;
    private String message;
    private ChatMessages.messageType messageType;
}
