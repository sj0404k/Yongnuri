package yongin.Yongnuri._Campus.dto;

import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Reports;

import java.time.LocalDateTime;

public class ReportReq {
    @Setter
    @Getter
    public static class reportDto {
        private Long reportedId;
        private Reports.ChatType postType;
        private Long postId;
        private Reports.ReportType reason;
        private String content;
        private boolean isImages;
    }
    @Setter
    @Getter
    public static class reportProcess {
        private Long reportedId;
        private Reports.ReportStatus status;
    }
}
