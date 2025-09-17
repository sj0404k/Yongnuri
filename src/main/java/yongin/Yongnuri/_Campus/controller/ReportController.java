package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yongin.Yongnuri._Campus.dto.ReportReq;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.ReportService;

@RestController
@RequestMapping("/report")
@AllArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> setReports(@AuthenticationPrincipal CustomUserDetails user, @RequestBody ReportReq.reportDto reportReq){
        reportService.reports(user, reportReq);
         return ResponseEntity.ok("Reports 성공");
    }
}
