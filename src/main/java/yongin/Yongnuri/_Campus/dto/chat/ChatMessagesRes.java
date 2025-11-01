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
    private ChatMessages.messageType chatType;  // 'text' | 'img' 등
    private String message;                     // 본문(텍스트 or 이미지 URL 등)
    private Long senderId;                      // ✅ 보낸 사람 ID (항상 포함)
    private String senderEmail;                 // ✅ 소문자 이메일 (항상 포함)
    private LocalDateTime createdAt;            // ISO 직렬화 가능 타입
}
