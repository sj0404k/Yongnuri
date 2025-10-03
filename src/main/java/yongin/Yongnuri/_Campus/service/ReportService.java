package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.Reports;
import yongin.Yongnuri._Campus.dto.ReportReq;
import yongin.Yongnuri._Campus.repository.ReportRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.domain.Enum;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    public void reports(CustomUserDetails user, ReportReq.reportDto reportReq) {
        Reports reports = Reports.builder()
                .reportId(user.getUser().getId())
                .reportedId(reportReq.getReportedId())
                .postId(reportReq.getPostId())
                .postType(reportReq.getPostType())
                .reason(reportReq.getReason())
                .content(reportReq.getContent())
//                .isImages(false)  // 아직 처리 미정 병합후 처리 해야됨
                .createdAt(LocalDateTime.now())
                .status(Enum.ReportStatus.PENDING)
                .build();
        reportRepository.save(reports);
    }

}
