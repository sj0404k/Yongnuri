package yongin.Yongnuri._Campus.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AdminReportRes2 {
    private Long id;
    private int reportedStudentId;
    private String reportedStudentName;
    private String content;
}
