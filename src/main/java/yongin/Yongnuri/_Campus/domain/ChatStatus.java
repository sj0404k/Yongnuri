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

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatStatus {
    /**
     * 채팅방을 봤는지 알아보기 위한 도매인
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatRoomId;
    private Long userId;                //해당 본인 유저 id값
    private LocalDateTime firstDate;    // 체팅방 처음 접속일 일단 넣어봤음
    private LocalDateTime lastDate;     //마지막 체팅 본기록 체팅방 눌렀을때 최신화하기
    private boolean chatStatus;         //true 채팅방 활성화 // false 비활성화

}
