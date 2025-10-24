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
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.repository.LostItemRepository;
import yongin.Yongnuri._Campus.repository.GroupBuyRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.domain.Enum;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final UsedItemRepository usedItemRepository;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;

    @Transactional
    public Long reports(CustomUserDetails user, ReportReq.reportDto reportReq) {
        Long reportedId = reportReq.getReportedId();

        // 1) reportedId 없으면 postType + postId로 작성자 ID 역추적
        if (reportedId == null) {
            Enum.ChatType postType = reportReq.getPostType();
            Long postId = reportReq.getPostId();

            if (postType != null && postId != null) {
                switch (postType) {
                    case USED_ITEM:
                        reportedId = resolveAuthorIdByReflection(
                                usedItemRepository.findById(postId));
                        break;
                    case LOST_ITEM:
                        reportedId = resolveAuthorIdByReflection(
                                lostItemRepository.findById(postId));
                        break;
                    case GROUP_BUY:
                        reportedId = resolveAuthorIdByReflection(
                                groupBuyRepository.findById(postId));
                        break;
                    // 공지/전체/관리자/채팅은 소유자 개념이 애매 → reportedId 반드시 필요
                    case ALL:
                    case ADMIN:
                    case Chat:
                    default:
                        break;
                }
            }
        }

        if (reportedId == null) {
            // 기존 동작 유지(프론트가 보던 400 메시지) — 글로벌 핸들러가 400 매핑 중
            throw new IllegalArgumentException("신고 대상 사용자 ID(reportedId)를 찾을 수 없습니다.");
        }

        // 2) 피신고자 존재 확인 (람다 대신 if로: final 경고 회피)
        Optional<User> reportedUserOpt = userRepository.findById(reportedId);
        if (!reportedUserOpt.isPresent()) {
            throw new RuntimeException("해당 유저가 없습니다. id=" + reportedId);
        }
        User reportedUser = reportedUserOpt.get();

        // 3) 저장
        boolean isImagesPresent = reportReq.getImageUrls() != null && !reportReq.getImageUrls().isEmpty();

        Reports newReport = Reports.builder()
                .reportId(user.getUser().getId())         // 신고자
                .reportedId(reportedId)                   // 피신고자
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

        return reportedUser.getId();
    }

    /**
     * 엔티티에서 작성자 ID를 안전하게 추출한다.
     * - 우선 getUserId(): Long 시도
     * - 다음 getUser().getId(): Long 시도
     * - 어느 쪽도 없으면 null
     */
    private Long resolveAuthorIdByReflection(Optional<?> entityOpt) {
        if (!entityOpt.isPresent()) return null;
        Object entity = entityOpt.get();

        // Case 1) getUserId(): Long
        try {
            Method m = entity.getClass().getMethod("getUserId");
            Object v = m.invoke(entity);
            if (v instanceof Long) return (Long) v;
        } catch (NoSuchMethodException ignore) {
        } catch (Exception ignore) {}

        // Case 2) getUser().getId(): Long
        try {
            Method mUser = entity.getClass().getMethod("getUser");
            Object userObj = mUser.invoke(entity);
            if (userObj != null) {
                Method mId = userObj.getClass().getMethod("getId");
                Object idObj = mId.invoke(userObj);
                if (idObj instanceof Long) return (Long) idObj;
            }
        } catch (NoSuchMethodException ignore) {
        } catch (Exception ignore) {}

        return null;
    }
}
