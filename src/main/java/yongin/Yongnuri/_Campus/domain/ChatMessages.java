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
    /**
     *주고 받은 매시지들이 보관되는 장소
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatRoomId;    //채팅방의 방 번호
    private messageType chatType;  //채팅 타입
    private String message;     //메시지 타입에 따른 결과 다름 택스트 or url
    private Long senderId;      //보낸 사람
    private LocalDateTime createdAt;    //생성일 이걸로 유저의 채팅 확인했는지 알아봄 비교 대상임

    public enum messageType {
        이미지,
        텍스트
    }
}
