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
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    private Long typeId;
    private Long toUserId;
    private Long fromUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String lastMessage;
    private int status;                 //뭐였더라...

    public enum ChatType {
        전체,
        중고,
        분실,
        공동구매
    }
}
