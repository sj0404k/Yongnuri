package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/*
채팅 관련 파일들 이름
WebSocketConfig
ChatController
ChatMessages
ChatRoom
chatStatus
ChatMessageRequest
*/
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private String message;
    private String sender;
    private LocalDateTime createdAt;
}
