package yongin.Yongnuri._Campus.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Reports;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AdminReportRes {
    private Long id;
    private int reportStudentId;
    private String reportStudentName;
    private Long reportCount;
    private Reports.ChatType reportType;
    private Long typeId;
    private String major;
    private Long reportId;
    private Long reportedId;

    private String reason;
    private String content;
    private LocalDateTime processedAt;
    private Reports.ReportStatus status;

}
