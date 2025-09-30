package yongin.Yongnuri._Campus.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Reports;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
public class AdminReq {
    @Setter
    @Getter
    public static class reportProcessReq {
        private Long id;
//        private Long userId;
        private Reports.ReportStatus reportStatus;
    }
    @Setter
    @Getter
    public static class reportProcessUserReq {
//        private Long id;
        private Long userId;
        private Reports.ReportStatus reportStatus;
    }

    // 공지사항 작성

    @Getter
    @Setter
    public static class NoticeCreateReqDto {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;
        @NotBlank(message = "내용은 필수입니다.")
        private String content;
        @NotBlank(message = "상태는 필수입니다.")
        private String status;
        private String link;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<String> imageUrls;
    }

 //공지사항 수정
    @Getter
    @Setter
    public static class NoticeUpdateReqDto {
        @NotNull(message = "수정할 게시글 ID는 필수입니다.")
        private Long noticeId;
        private String title;
        private String content;
        private String status;
        private String link;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<String> imageUrls;
    }
}
