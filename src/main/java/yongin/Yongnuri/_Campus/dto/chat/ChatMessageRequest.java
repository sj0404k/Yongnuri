package yongin.Yongnuri._Campus.dto.chat; // 새로운 DTO 패키지

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yongin.Yongnuri._Campus.domain.ChatMessages;
import yongin.Yongnuri._Campus.domain.ChatRoom;

import java.util.List;

// 클라이언트로부터 메시지를 받을 때 사용하는 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private Long roomId;

    private ChatMessages.messageType type; //img, txt
    private String message;

    private List<String> imageUrls; // ✅ 이미지 URL 리스트
}