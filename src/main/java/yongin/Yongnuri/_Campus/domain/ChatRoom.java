package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom {

    /**
     * 채팅방의 첫 메인 공간
     * 채팅방 생성 시 자동으로 만들어짐
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 채팅방 고유 ID (메시지 등 조회용)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enum.ChatType type;  // 채팅방 종류 (전체, 중고, 분실, 공동구매 등)

    @Column(nullable = false)
    private Long typeId;  // 파생된 대상 ID (예: 게시글 ID 등)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    /**
     * 채팅방에 속한 메시지 목록
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessages> messages;

    /**
     * 채팅방에 참여 중인 유저 상태 (읽음, 접속시간 등)
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatStatus> participants;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }
    public enum RoomStatus {
        ACTIVE,     // 활성
        INACTIVE    // 비활성 (삭제됨)
    }
/*
    public enum ChatType {
        전체,
        중고,
        분실,
        공동구매,
        관리자
    }
    */

}
//    // 해당 부분이 필요한가?
//    private Long toUserId;          // 주로 게시글 작성자?
//    private Long fromUserId;        //
//   private String lastMessage;