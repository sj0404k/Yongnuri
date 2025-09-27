package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notice")
public class Notice {
    /**@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isImages;

    @Enumerated(EnumType.STRING)
    private NoticeStatus status;

    private String link;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 상태 값을 정의하는 Enum
    public enum NoticeStatus {
        RECRUITING, // 모집중
        COMPLETED,  // 모집완료
        DELETED     // 삭제됨
    }*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isImages;

    // (수정) String -> NoticeStatus 타입으로 변경하고 @Enumerated 추가
    @Enumerated(EnumType.STRING)
    private NoticeStatus status;

    private String link;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // (추가) 상태 값을 정의하는 Enum
    public enum NoticeStatus {
        RECRUITING, // 모집중/진행중
        COMPLETED,  // 완료/마감
        DELETED     // 삭제됨
    }
}