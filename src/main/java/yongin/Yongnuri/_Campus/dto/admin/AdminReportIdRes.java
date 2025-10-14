package yongin.Yongnuri._Campus.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Reports;
import yongin.Yongnuri._Campus.domain.Enum;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@Builder
public class AdminReportIdRes {
    private Long id;        // 리폿 ID

    private int reportedStudentId;
    private String reportedStudentName;
    private String reportedStudentNickName;
    private Enum.ReportReason reason;
    private String content;
    //게시판 관련 id?
    private Enum.ChatType reportType;
    private Long typeId;
    private List<ImageDto> images;

}
