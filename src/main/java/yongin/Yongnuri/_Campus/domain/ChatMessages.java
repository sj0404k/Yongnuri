package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 주고받은 메시지를 저장하는 엔티티
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 해당 메시지가 속한 채팅방
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * 메시지 타입 (텍스트 / 이미지)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private messageType chatType;

    /**
     * 메시지 내용 (텍스트 or 이미지 URL)
     */
    @Column(columnDefinition = "TEXT")
    private String message;
    private List<String> imageUrls; // ✅ 이미지 URL 리스트
    /**
     * 메시지 보낸 사람
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * 메시지 생성 시각
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum messageType {
        img,
        text
    }
}
