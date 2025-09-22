package yongin.Yongnuri._Campus.admin;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.domain.Reports;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.admin.AdminReportRes;
import yongin.Yongnuri._Campus.dto.admin.AdminReq;
import yongin.Yongnuri._Campus.dto.admin.UserInfoRes;
import yongin.Yongnuri._Campus.repository.ReportRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public List<AdminReportRes> getReportList(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
        if (!User.Role.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        List<Object[]> results = reportRepository.findReportCountsGrouped();

        return results.stream()
                .map(obj -> {
                    Long reportedId = (Long) obj[0];
                    Long count = (Long) obj[1];

                    User reportedUser = userRepository.findById(reportedId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

                    return AdminReportRes.builder()
                            .reportStudentId(reportedUser.getStudentId()) // 학생 학번
                            .reportStudentName(reportedUser.getName())   // 이름
                            .reportCount(count)
                            .major(reportedUser.getMajor())// 신고 횟수
                            .build();
                })
                .collect(Collectors.toList());
    }
    public List<AdminReportRes> getReportListDetail(String email, Long reportedId) {
        // 1. 관리자 확인
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
        if (!User.Role.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2. 신고 내역 조회 (특정 유저)
        List<Reports> reports = reportRepository.findByReportedId(reportedId); // <- Repository에 메소드 필요

        if (reports.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저의 신고 내역이 없습니다.");
        }

        // 3. 신고당한 유저 정보
        User reportedUser = userRepository.findById(reportedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 4. DTO 변환
        return reports.stream()
                .map(report -> AdminReportRes.builder()
                        .reportStudentId(reportedUser.getStudentId()) // 학생 학번
                        .reportStudentName(reportedUser.getName())   // 이름
                        .major(reportedUser.getMajor())             // 전공
                        .reportType(report.getPostType())           // 신고 타입
                        .typeId(report.getPostId())                 // 신고 대상 ID
                        .build()
                )
                .collect(Collectors.toList());
    }


    public List<UserInfoRes> getAllUserInfo(String adminEmail) {
        // 1. 관리자 확인
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));

        if (!User.Role.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2. 전체 유저 조회
        List<User> users = userRepository.findAll();

        // 3. DTO 변환 (신고 횟수 포함)
        return users.stream()
                .map(user -> {
                    Long reportCount = reportRepository.countByReportedId(user.getId());
                    return UserInfoRes.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .studentId(user.getStudentId())
                            .major(user.getMajor())
                            .reportCount(reportCount)
                            .build();
                })
                .collect(Collectors.toList());
    }


    public boolean processReport(String email, AdminReq.reportProcessReq req) {
        // 1. 관리자 확인
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));

        if (!User.Role.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2. 신고 조회
        Reports report = reportRepository.findById(req.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."));

        // 3. 상태 변경
        report.setStatus(req.getReportStatus()); // AdminReq.reportProcessReq에 status 필드 필요
        report.setProcessedAt(LocalDateTime.now());
        // 4. DB 저장
        reportRepository.save(report);

        return true; // 성공적으로 처리됨
    }

    public boolean processReportUser(String email, AdminReq.reportProcessUserReq req) {
        // 1. 관리자 확인
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));

        if (!User.Role.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 2. 신고당한 유저의 모든 신고 조회
        List<Reports> reports = reportRepository.findByReportedId(req.getUserId());
        if (reports.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다.");
        }

        // 3. 상태 변경
        reports.forEach(report -> {
            report.setStatus(req.getReportStatus()); // 상태 변경
            report.setProcessedAt(LocalDateTime.now());    // 처리 일시 업데이트
        });

        // 4. DB 저장
        reportRepository.saveAll(reports);

        return true; // 성공적으로 처리됨
    }
}