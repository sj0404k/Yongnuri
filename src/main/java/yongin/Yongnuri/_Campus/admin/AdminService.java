package yongin.Yongnuri._Campus.admin;

import jakarta.persistence.EntityNotFoundException;
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
import java.util.stream.Collectors;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.security.CustomUserDetails;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    private final ImageRepository imageRepository;



//    public List<AdminReportRes2> getReportList1(String email) {
//        // 1. 관리자 확인
//        User admin = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
//
//        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
//        }
//
//        // 2. 모든 신고 조회
//        List<Reports> reports = reportRepository.findAll();
//
//        // 3. DTO 변환
//        return reports.stream()
//                .map(report -> {
//                    User reportedUser = userRepository.findById(report.getReportedId())
//                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피신고 유저를 찾을 수 없습니다."));
//
//                    String truncatedContent = report.getContent();
//                    if (truncatedContent != null && truncatedContent.length() > 30) {
//                        truncatedContent = truncatedContent.substring(0, 30) + "..."; // 잘린 표시
//                    }
//
//                    return AdminReportRes2.builder()
//                            .id(report.getId())
//                            .reportedStudentId(reportedUser.getStudentId())
//                            .reportedStudentName(reportedUser.getName())
//                            .content(truncatedContent)
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//    }

    /**
     *
     * @param email: 신고 리스트 보여줌
     * @return : reportStudentNickName, reportReason, content, reportType, typeId, status
     */
    public List<AdminReportRes> getReportList(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
        }

        // 모든 신고 + 유저 정보 조회
        List<Object[]> results = reportRepository.findAllReportsWithUser();

        // DTO 변환
        return results.stream()
                .map(obj -> {
                    Reports report = (Reports) obj[0];
                    User reportedUser = (User) obj[1];
                    String content = report.getContent();
                    if (content != null && content.length() > 20) {
                        content = content.substring(0, 20) + "...";
                    }
                    return AdminReportRes.builder()
                            .reportStudentNickName(reportedUser.getNickName())
                            .reportReason(report.getReportReason())
                            .content(content)
                            .reportType(report.getPostType())
                            .typeId(report.getPostId())
                            .status(report.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }
//    public List<AdminReportRes> getReportListDetail(String email, Long reportedId) {
//        // 1. 관리자 확인
//        User admin = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인하세요."));
//        if (!Enum.UserRole.ADMIN.equals(admin.getRole())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다.");
//        }
//
//        // 2. 신고 내역 조회 (특정 유저)
//        List<Reports> reports = reportRepository.findByReportedId(reportedId); // <- Repository에 메소드 필요
//
//        if (reports.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저의 신고 내역이 없습니다.");
//        }
//
//        // 3. 신고당한 유저 정보
//        User reportedUser = userRepository.findById(reportedId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
//
//        // 4. DTO 변환
//        return reports.stream()
//                .map(report -> AdminReportRes.builder()
//                        .reportStudentId(reportedUser.getStudentId()) // 학생 학번
//                        .reportStudentName(reportedUser.getName())   // 이름
////                        .major(reportedUser.getMajor())             // 전공
//                        .reportType(report.getPostType())           // 신고 타입
//                        .typeId(report.getPostId())                 // 신고 대상 ID
//                        .build()
//                )
//                .collect(Collectors.toList());
//    }

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
            report.setStatus(req.getReportStatus()); // 상태 변경
            report.setProcessedAt(LocalDateTime.now());    // 처리 일시 업데이트
        });

        // 4. DB 저장
        reportRepository.saveAll(reports);

        return true; // 성공적으로 처리됨
    }

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
                .reportedStudentId(reportedUser.getStudentId())   // User 엔티티의 학번
                .reportedStudentName(reportedUser.getName())      // User 엔티티의 이름
                .reason(report.getReportReason())
                .content(report.getContent())                     // 신고 내용
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
    /**
    // 공지사항 작성
    public Long createNotice(String adminEmail, AdminReq.NoticeCreateReqDto requestDto) { // <-- 타입을 NoticeCreateReqDto로 변경
        User admin = getAdminUser(adminEmail);

        Notice newNotice = Notice.builder()
                .author(admin)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .status(Enum.NoticeStatus.valueOf(requestDto.getStatus().toUpperCase()))
                .link(requestDto.getLink())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .isImages(requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty())
                .createdAt(LocalDateTime.now())
                .build();

        Notice savedNotice = noticeRepository.save(newNotice);
        Long newPostId = savedNotice.getId();

        if (savedNotice.getIsImages()) {
            int sequence = 1;
            for (String url : requestDto.getImageUrls()) {
                imageRepository.save(Image.builder().type("NOTICE").typeId(newPostId).imageUrl(url).sequence(sequence++).build());
            }
        }
        return newPostId;
    }

     //공지사항 수정
     @Transactional
     public void updateNotice(AdminReq.NoticeUpdateReqDto requestDto) {
         Notice notice = noticeRepository.findById(requestDto.getNoticeId())
                 .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
         if(requestDto.getTitle() != null) notice.setTitle(requestDto.getTitle());
         if(requestDto.getContent() != null) notice.setContent(requestDto.getContent());
         if(requestDto.getStatus() != null) notice.setStatus(Enum.NoticeStatus.valueOf(requestDto.getStatus().toUpperCase()));
         if(requestDto.getLink() != null) notice.setLink(requestDto.getLink());
         if(requestDto.getStartDate() != null) notice.setStartDate(requestDto.getStartDate());
         if(requestDto.getEndDate() != null) notice.setEndDate(requestDto.getEndDate());

     }
     //공지사항 삭제
     @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));

        notice.setStatus(Enum.NoticeStatus.DELETED);
    }*/
    @Transactional
    public void postNotice(CustomUserDetails user, String text) {
        getAdminUser(user.getUser().getEmail());
        User user1 = user.getUser();
        user1.setText(text);
        userRepository.save(user1);

    }
}