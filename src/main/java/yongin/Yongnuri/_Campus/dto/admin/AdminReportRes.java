package yongin.Yongnuri._Campus.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Reports;
import yongin.Yongnuri._Campus.domain.Enum;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AdminReportRes {
    private Long id;
    private int reportStudentId;
    private String reportStudentName;
    private String reportStudentNickName;
//    private Long reportCount;
    private Enum.ChatType reportType;
    private Long typeId;
//    private String major;
    private Long reportId;
    private Long reportedId;

    private Enum.ReportReason reportReason;
    private String content;
    private LocalDateTime processedAt;
    private Enum.ReportStatus status;

}
