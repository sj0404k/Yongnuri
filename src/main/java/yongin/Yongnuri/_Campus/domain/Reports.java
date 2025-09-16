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
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportId;
    private Long reportedId;
    private ChatType postType;
    private Long postId;
    private String reason;
    private String content;
    private boolean isImages;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private ReportStatus status;

    public enum ChatType {
        전체,
        중고,
        분실,
        공동구매
    }

    public enum ReportStatus {
        처리대기,
        처리완료,
    }


}
