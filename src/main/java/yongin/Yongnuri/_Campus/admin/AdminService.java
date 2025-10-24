// src/main/java/yongin/Yongnuri/_Campus/admin/AdminService.java
package yongin.Yongnuri._Campus.admin;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.Reports;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.admin.*;
import yongin.Yongnuri._Campus.repository.ReportRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.repository.ImageRepository;

import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.repository.LostItemRepository;
import yongin.Yongnuri._Campus.repository.GroupBuyRepository;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.security.CustomUserDetails;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ImageRepository imageRepository;

    // 게시글 상태 변경용
    private final UsedItemRepository usedItemRepository;
    private final LostItemRepository lostItemRepository;
    private final GroupBuyRepository groupBuyRepository;

    /** 신고 목록 */
    @Transactional(readOnly = true)
    public List<AdminReportRes> getReportList(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        List<Reports> results = reportRepository.findAllByOrderByCreatedAtDesc();

        return results.stream().map(report -> {
            Long reportedId = report.getReportedId();
            String nickName = "탈퇴한 사용자";
            if (reportedId != null) {
                Optional<User> reportedUserOpt = userRepository.findById(reportedId);
                if (reportedUserOpt.isPresent()) {
                    User u = reportedUserOpt.get();
                    String n = u.getNickName();
                    if (n == null || n.trim().isEmpty()) n = u.getName();
                    if ((n == null || n.trim().isEmpty()) && u.getEmail() != null) {
                        n = u.getEmail().split("@")[0];
                    }
                    if (n != null && !n.trim().isEmpty()) nickName = n;
                }
            }

            String content = report.getContent();
            if (content != null && content.length() > 20) content = content.substring(0, 20) + "...";

            return AdminReportRes.builder()
                    .id(report.getId())
                    .reportStudentNickName(nickName)
                    .reportReason(report.getReportReason())
                    .content(content)
                    .reportType(report.getPostType())
                    .typeId(report.getPostId())
                    .status(report.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    /** 전체 유저 + 신고 승인 횟수 (관리자 제외) */
    public List<UserInfoRes> getAllUserInfo(String adminEmail) {
        // 1) 관리자 검증
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2) 일반 유저만 조회(ADMIN 제외)
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() != Enum.UserRole.ADMIN)
                .collect(Collectors.toList());

        // 3) DTO 변환 (승인된 신고 횟수 포함)
        return users.stream()
                .map(user -> {
                    Long reportCount = reportRepository.countByReportedIdAndStatus(user.getId(), Enum.ReportStatus.APPROVED);
                    return UserInfoRes.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .userNickname(user.getNickName())
                            .studentId(user.getStudentId())
                            .major(user.getMajor())
                            .reportCount(reportCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 신고 처리 (APPROVED / REJECTED) */
    @Transactional
    public boolean processReport(String email, AdminReq.reportProcessReq req) {
        // 1. 관리자 확인
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));

        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2. 신고 조회
        Reports report = reportRepository.findById(req.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."));
        // 3. 상태 변경
        Enum.ReportStatus next = req.getReportStatus();
        report.setStatus(next);
        report.setProcessedAt(LocalDateTime.now());
        // 4. DB 저장
        reportRepository.save(report);

        if (Enum.ReportStatus.APPROVED.equals(next)) {
            // 인정 → 비노출/해결 처리
            softDeletePostIfNeeded(report);
        } else if (Enum.ReportStatus.REJECTED.equals(next)) {
            // 미인정 → 다시 노출(활성화)
            reactivatePostIfNeeded(report);
        }
        return true;
    }

    /** 동일 유저 대상 신고 일괄 처리 */
    @Transactional
    public boolean processReportUser(String email, AdminReq.reportProcessUserReq req) {
        // 1. 관리자 확인
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));

        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2. 신고당한 유저의 모든 신고 조회
        List<Reports> reports = reportRepository.findByReportedId(req.getUserId());
        if (reports.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다.");
        }
        // 3. 상태 변경
        reports.forEach(r -> {
            r.setStatus(req.getReportStatus());
            r.setProcessedAt(LocalDateTime.now());
        });

        // 4. DB 저장
        reportRepository.saveAll(reports);

        // 필요하면 일괄 활성/비활성도 여기서 돌릴 수 있음
        return true;
    }

    /** 신고 상세 */
    public AdminReportIdRes getReportDetail(Long reportId) {
        // 1. 신고 조회
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다."));

        // 2. 피신고 유저 조회
        User reportedUser = userRepository.findById(report.getReportedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피신고 유저를 찾을 수 없습니다."));

        //이미지리수트
        List<Image> images = List.of();
        if (Boolean.TRUE.equals(report.getIsImages())) {
            images = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc("REPORT", reportId);
        }
        List<ImageDto> imageDtos = images.stream().map(ImageDto::new).collect(Collectors.toList());

        // 3. DTO 변환
        return AdminReportIdRes.builder()
                .id(report.getId())
                .reportedStudentId(reportedUser.getStudentId())
                .reportedStudentName(reportedUser.getName())
                .reason(report.getReportReason())
                .content(report.getContent())
                .images(imageDtos)
                .build();
    }

    @Transactional
    protected User getAdminUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
        if (user.getRole() != Enum.UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }
        return user;
    }

    @Transactional
    public void postNotice(CustomUserDetails user, String text) {
        getAdminUser(user.getUser().getEmail());
        User user1 = user.getUser();
        user1.setText(text);
        userRepository.save(user1);
    }

    /* ================== 내부 유틸 ================== */

    /** 인정 시: 게시글 비노출/해결 처리 */
    private void softDeletePostIfNeeded(Reports report) {
        if (report.getPostId() == null || report.getPostType() == null) return;

        Long postId = report.getPostId();
        Enum.ChatType type = report.getPostType();

        switch (type) {
            case USED_ITEM:
                usedItemRepository.findById(postId).ifPresent(item -> {
                    Enum.UsedItemStatus s = firstEnumOrNull(Enum.UsedItemStatus.class,
                            "DELETED", "HIDDEN", "REMOVED", "SOLD");
                    if (s != null) { item.setStatus(s); usedItemRepository.save(item); }
                });
                break;

            case LOST_ITEM:
                lostItemRepository.findById(postId).ifPresent(lost -> {
                    Enum.LostItemStatus s = firstEnumOrNull(Enum.LostItemStatus.class,
                            "RESOLVED", "CLOSED", "DELETED", "RETURNED", "COMPLETED");
                    if (s != null) { lost.setStatus(s); lostItemRepository.save(lost); }
                });
                break;

            case GROUP_BUY:
                groupBuyRepository.findById(postId).ifPresent(gb -> {
                    Enum.GroupBuyStatus s = firstEnumOrNull(Enum.GroupBuyStatus.class,
                            "DELETED", "COMPLETED", "CLOSED");
                    if (s != null) { gb.setStatus(s); groupBuyRepository.save(gb); }
                });
                break;

            case ALL:
            case ADMIN:
            case Chat:
            default:
                break;
        }
    }

    /** 미인정 시: 게시글 재노출(활성화) 처리 */
    private void reactivatePostIfNeeded(Reports report) {
        if (report.getPostId() == null || report.getPostType() == null) return;

        Long postId = report.getPostId();
        Enum.ChatType type = report.getPostType();

        switch (type) {
            case USED_ITEM:
                usedItemRepository.findById(postId).ifPresent(item -> {
                    Enum.UsedItemStatus s = firstEnumOrNull(Enum.UsedItemStatus.class,
                            "ON_SALE", "ACTIVE", "OPEN");
                    if (s != null) { item.setStatus(s); usedItemRepository.save(item); }
                });
                break;

            case LOST_ITEM:
                lostItemRepository.findById(postId).ifPresent(lost -> {
                    Enum.LostItemStatus s = firstEnumOrNull(Enum.LostItemStatus.class,
                            "OPEN", "ACTIVE");
                    if (s != null) { lost.setStatus(s); lostItemRepository.save(lost); }
                });
                break;

            case GROUP_BUY:
                groupBuyRepository.findById(postId).ifPresent(gb -> {
                    Enum.GroupBuyStatus s = firstEnumOrNull(Enum.GroupBuyStatus.class,
                            "RECRUITING", "OPEN", "ACTIVE");
                    if (s != null) { gb.setStatus(s); groupBuyRepository.save(gb); }
                });
                break;

            case ALL:
            case ADMIN:
            case Chat:
            default:
                break;
        }
    }

    /** 주어진 이름들 중 첫 번째로 존재하는 Enum 상수를 반환 (없으면 null) */
    private static <E extends java.lang.Enum<E>> E firstEnumOrNull(Class<E> enumType, String... names) {
        for (String n : names) {
            try { return java.lang.Enum.valueOf(enumType, n); }
            catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static void safeSetStatus(Object entity, Object statusEnum) {
        if (entity == null || statusEnum == null) return;
        try {
            Method m = entity.getClass().getMethod("setStatus", statusEnum.getClass());
            m.invoke(entity, statusEnum);
        } catch (Exception ignored) {}
    }
}
