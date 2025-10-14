package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yongin.Yongnuri._Campus.domain.Enum.ChatType;
import yongin.Yongnuri._Campus.domain.Enum.ReportStatus;
import yongin.Yongnuri._Campus.domain.Enum.ReportReason;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;// 게시글 id

    private Long reportId;      // 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", referencedColumnName = "user_id")
    private User reportedUser;        //당한자
    private Long postId;        //게시글 id
    private String content;     //신고내용
    private Boolean isImages;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    @Enumerated(EnumType.STRING)
    private ChatType postType;  //게시글 타입
    @Enumerated(EnumType.STRING)
    private Enum.ReportReason reportReason;      //이유
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
}

/**
    public enum ReportType{
        도배,
        홍보_광고행위,
        음란성게시물,
        상대방비방및혐오,
        사칭및거짓정보,
        기타
    }

    public enum ChatType {
        전체,
        중고,
        분실,
        공동구매
    }

    public enum ReportStatus {
        처리대기,
        처리반려,
        처리승인,
    }
*/


