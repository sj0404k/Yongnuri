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
public class AdminReportIdRes {
    private Long id;        // 리폿 ID

    private int reportedStudentId;
    private String reportedStudentName;
    private Reports.ReportType reason;
    private String content;
    //게시판 관련 id?
    private Reports.ChatType reportType;
    private Long typeId;

}
