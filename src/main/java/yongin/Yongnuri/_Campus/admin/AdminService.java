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

    /**
     * 관리자 신고 목록
     * - reportedId 가 null 이거나 잘못된 경우에도 NPE 없이 동작
     * - 최신 생성순 정렬
     * @return reportStudentNickName, reportReason, content(20자 내 잘라 표시), reportType, typeId, status
     */
    @Transactional(readOnly = true)
    public List<AdminReportRes> getReportList(String email) {
        // 1) 관리자 권한 확인
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2) 신고 목록(최신순)
        List<Reports> results = reportRepository.findAllByOrderByCreatedAtDesc();

        // 3) DTO 변환 (NULL-safe)
        return results.stream().map(report -> {
            Long reportedId = report.getReportedId(); // null 가능

            String nickName = "탈퇴한 사용자";
            if (reportedId != null) {
                // ⚠️ null 이면 findById 호출하지 않음
                Optional<User> reportedUserOpt = userRepository.findById(reportedId);
                if (reportedUserOpt.isPresent()) {
                    User u = reportedUserOpt.get();
                    // 닉네임 → 없으면 이름 → 없으면 메일 로컬파트
                    String n = u.getNickName();
                    if (n == null || n.trim().isEmpty()) n = u.getName();
                    if ((n == null || n.trim().isEmpty()) && u.getEmail() != null) {
                        n = u.getEmail().split("@")[0];
                    }
                    if (n != null && !n.trim().isEmpty()) nickName = n;
                }
            }

            String content = report.getContent();
            if (content != null && content.length() > 20) {
                content = content.substring(0, 20) + "...";
            }

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

    /**
     *
     * @param adminEmail :
     * @return id, name, userNickname, studentId, major, reportCount
     */
    public List<UserInfoRes> getAllUserInfo(String adminEmail) {
        // 1. 관리자 확인
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));

        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2. 전체 유저 조회
        List<User> users = userRepository.findAll();

        // 3. DTO 변환 (신고 횟수 포함)
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
        report.setStatus(req.getReportStatus());
        report.setProcessedAt(LocalDateTime.now());

        // 4. DB 저장
        reportRepository.save(report);

        return true;
    }

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
        reports.forEach(report -> {
            report.setStatus(req.getReportStatus());
            report.setProcessedAt(LocalDateTime.now());
        });

        // 4. DB 저장
        reportRepository.saveAll(reports);

        return true;
    }

    public AdminReportIdRes getReportDetail(Long reportId) {
        // 1. 신고 조회
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다."));

        // 2. 피신고 유저 조회
        User reportedUser = userRepository.findById(report.getReportedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피신고 유저를 찾을 수 없습니다."));

        // 3. 이미지 조회
        List<Image> images = List.of();
        if (Boolean.TRUE.equals(report.getIsImages())) {
            images = imageRepository.findByTypeAndTypeIdOrderBySequenceAsc("REPORT", reportId);
        }
        List<ImageDto> imageDtos = images.stream().map(ImageDto::new).collect(Collectors.toList());

        // 4. DTO 변환
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
}
