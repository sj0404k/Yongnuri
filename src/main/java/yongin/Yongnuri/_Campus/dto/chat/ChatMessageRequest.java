package yongin.Yongnuri._Campus.dto.chat; // 새로운 DTO 패키지

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 클라이언트로부터 메시지를 받을 때 사용하는 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private String sender;
    private String type;
    private String content;
//    private Long timestamp;
}