package yongin.Yongnuri._Campus.dto;

import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Reports;
import yongin.Yongnuri._Campus.domain.Enum;
import java.time.LocalDateTime;
import java.util.List;
public class ReportReq {
    @Setter
    @Getter
    public static class reportDto {
        private Long reportedId;
        private Enum.ChatType postType;
        private Long postId;
        private Enum.ReportReason reason;
        private String content;
        private List<String> imageUrls;
    }
    @Setter
    @Getter
    public static class reportProcess {
        private Long reportedId;
        private Enum.ReportStatus status;
    }
}

