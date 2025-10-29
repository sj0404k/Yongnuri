package yongin.Yongnuri._Campus.dto.chat; // 새로운 DTO 패키지

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yongin.Yongnuri._Campus.domain.ChatMessages;
import yongin.Yongnuri._Campus.domain.ChatRoom;

// 클라이언트로부터 메시지를 받을 때 사용하는 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
//    private String sender;
    private ChatMessages.messageType type;
    private String message;
//    private Long timestamp;
}