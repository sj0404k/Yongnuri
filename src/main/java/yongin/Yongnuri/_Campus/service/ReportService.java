package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.Reports;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.ReportReq;
import yongin.Yongnuri._Campus.repository.ImageRepository;
import yongin.Yongnuri._Campus.repository.ReportRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.domain.Enum;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    @Transactional
    public boolean reports(CustomUserDetails user, ReportReq.reportDto reportReq) {

        boolean isImagesPresent = reportReq.getImageUrls() != null && !reportReq.getImageUrls().isEmpty();
        User reportedUser = userRepository.findById(reportReq.getReportedId())
                .orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));
        Reports newReport = Reports.builder()
                .reportId(user.getUser().getId())
                .reportedUser(reportedUser)
                .postId(reportReq.getPostId())
                .postType(reportReq.getPostType())
                .reportReason(reportReq.getReason())
                .content(reportReq.getContent())
                .isImages(isImagesPresent)
                .createdAt(LocalDateTime.now())
                .status(Enum.ReportStatus.PENDING)
                .build();
        Reports savedReport = reportRepository.save(newReport);

        if (isImagesPresent) {
            int sequence = 1;
            for (String url : reportReq.getImageUrls()) {
                imageRepository.save(Image.builder()
                        .type("REPORT")
                        .typeId(savedReport.getId())
                        .imageUrl(url)
                        .sequence(sequence++)
                        .build());
            }
        }
        return true;
    }
}
