package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatStatus {

    /**
     * 채팅방을 본 기록을 추적하는 엔티티
     * (읽음 여부, 마지막 접속시간 등)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 채팅방에 대한 상태인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * 어떤 유저의 상태인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime firstDate; // 처음 접속 시간
    private LocalDateTime lastDate;  // 마지막 접속 시간
    private boolean chatStatus;      // true = 활성화, false = 비활성화
}
