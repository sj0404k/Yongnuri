package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = "notice")
public class Notice {
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
    private LocalDateTime startDate; // 신청 시작일
    private LocalDateTime endDate;   // 신청 종료일
    private LocalDateTime createdAt;

    // (추가) 상태 값을 정의하는 Enum
    public enum NoticeStatus {
        RECRUITING, // 모집중/진행중
        COMPLETED,  // 완료/마감
        DELETED     // 삭제됨
    }
}

