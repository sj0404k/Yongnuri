package yongin.Yongnuri._Campus.dto.chat;

import lombok.*;
import yongin.Yongnuri._Campus.domain.ChatMessages;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessagesRes {
    /**
     * 채탕방 들어갈때 보여줘야 되는 틀
     */

    private ChatMessages.messageType chatType;  //채팅 타입
    private String message;                     //메시지 타입에 따른 결과 다름 택스트 or url
    private Long senderId;                      //보낸 사람
//    private int count;
    private LocalDateTime createdAt;
}
