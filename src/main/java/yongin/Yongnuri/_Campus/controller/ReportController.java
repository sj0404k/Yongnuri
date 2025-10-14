package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yongin.Yongnuri._Campus.dto.ReportReq;
import yongin.Yongnuri._Campus.dto.ReportRes;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.ReportService;

@RestController
@RequestMapping("/report")
@AllArgsConstructor
public class ReportController {
    private final ReportService reportService;

    /**
     *
     * @param user :
     * @param reportReq : reportedId, postType, postId, reason, content, imageUrls
     *     }
     * @return 상태갑
     */
    @PostMapping
    public ResponseEntity<?> setReports(@AuthenticationPrincipal CustomUserDetails user,
                                        @RequestBody ReportReq.reportDto reportReq) {
        Long reportedUserId = reportService.reports(user, reportReq);
        if (reportedUserId != null) {
            ReportRes response = new ReportRes("Reports 성공", reportedUserId);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("이미 신고한 게시글입니다.");
        }
    }
}
