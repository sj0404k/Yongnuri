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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
    private boolean isImges;

    private Boolean isImages;

    private String status;

    private String link;
    private LocalDateTime startDate; // 신청 시작일
    private LocalDateTime endDate;   // 신청 종료일
    private LocalDateTime createdAt;
}
