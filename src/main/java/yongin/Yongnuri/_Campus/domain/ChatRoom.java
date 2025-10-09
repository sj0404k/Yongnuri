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
    /**
     *채팅방의 첫 매인 공간
     * 채팅방 만들시 생성됨
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    //해당 부분으로 채팅방 메시지등 조회

    @Enumerated(EnumType.STRING)
    private Enum.ChatType type;
    private Long typeId;

    private Long toUserId;
    private Long fromUserId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String lastMessage;
    private int status;                 //뭐였더라... 아마 채팅방 삭제 유무 같았는데.....

//    public enum ChatType {
//        전체,
//        중고,
//        분실,
//        공동구매,
    //    관리자
//    }
}
