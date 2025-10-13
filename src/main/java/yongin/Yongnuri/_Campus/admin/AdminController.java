package yongin.Yongnuri._Campus.admin;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.admin.AdminReportIdRes;
import yongin.Yongnuri._Campus.dto.admin.AdminReq;
import yongin.Yongnuri._Campus.dto.admin.UserInfoRes;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    //문의사항 공지
    @GetMapping ("/notice")
    public ResponseEntity<?> getNotice (@AuthenticationPrincipal CustomUserDetails user){
        return ResponseEntity.ok(user.getUser().getText());
    }
    @PostMapping("/notice")
    public ResponseEntity<String> postNotice (@AuthenticationPrincipal CustomUserDetails user, @RequestBody String text){
        adminService.postNotice(user,text);
        return ResponseEntity.ok(user.getUser().getText());
    }
    /**신고 관리목록 가져오기 */
    @GetMapping("/reportList")
    public ResponseEntity<?> getReportList(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(adminService.getReportList(user.getUser().getEmail()));
    }
    /**신고 관리목록 가져오기 */
//    @GetMapping("/reportManagement")
//    public ResponseEntity<?> getReportList1(@AuthenticationPrincipal CustomUserDetails user) {
//        return ResponseEntity.ok(adminService.getReportList1(user.getUser().getEmail()));
//    }
//    @GetMapping("/reportList/{userId}")
//    public ResponseEntity<?> getReportListDetail(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("userId") Long reporedId) {
//        return ResponseEntity.ok(adminService.getReportListDetail(user.getUser().getEmail(),reporedId));
//    }
    @GetMapping("/userInfo")
    public ResponseEntity<List<UserInfoRes>> getAllUserInfo(@AuthenticationPrincipal CustomUserDetails user) {
        // 1. 관리자 권한 확인 후 모든 유저 정보 조회
        List<UserInfoRes> userInfoList = adminService.getAllUserInfo(user.getUser().getEmail());
        // 2. 조회 결과 반환
        return ResponseEntity.ok(userInfoList);
    }

    /** 신고 처리하기 */
    @PostMapping("/reportProcess")
    public ResponseEntity<?> processReport(@AuthenticationPrincipal CustomUserDetails user, @RequestBody AdminReq.reportProcessReq req) {
        // 1. 필수 값 검증
        if (req.getId() == null ||  req.getReportStatus() == null) {
            return ResponseEntity.badRequest().body("신고 ID, 상태값 모두 필요합니다.");
        }
        boolean processed = adminService.processReport(user.getUser().getEmail(), req);
        if (processed) {
            return ResponseEntity.ok("신고가 처리되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("신고 처리에 실패했습니다.");
        }
    }
    /** 신고 처리하기 */
    @PostMapping("/reportProcessUser")
    public ResponseEntity<?> processReportUser(@AuthenticationPrincipal CustomUserDetails user, @RequestBody AdminReq.reportProcessUserReq req) {
        // 1. 필수 값 검증
        if (req.getUserId() == null ||  req.getReportStatus() == null) {
            return ResponseEntity.badRequest().body("신고 유저 ID, 상태값 모두 필요합니다.");
        }
        boolean processed = adminService.processReportUser(user.getUser().getEmail(), req);
        if (processed) {
            return ResponseEntity.ok("신고가 처리되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("신고 처리에 실패했습니다.");
        }
    }

     //공지홍보 게시글 작성

    @PostMapping("/board")
    public ResponseEntity<?> createNotice(
            @AuthenticationPrincipal CustomUserDetails user,
            // (수정) NoticeCreateReqDto를 사용합니다.
            @Valid @RequestBody AdminReq.NoticeCreateReqDto requestDto) {
        Long noticeId = adminService.createNotice(user.getUser().getEmail(), requestDto);
        return ResponseEntity.ok(Map.of("message", "공지사항 작성 성공", "noticeId", noticeId));
    }

   //공지홍보 게시글 수정

    @PatchMapping("/board/rectify")
    public ResponseEntity<?> updateNotice(
            @Valid @RequestBody AdminReq.NoticeUpdateReqDto requestDto) {
        adminService.updateNotice(requestDto);
        return ResponseEntity.ok("공지사항 수정 성공");
    }


     //공지홍보 게시글 삭제
    @DeleteMapping("/board")
    public ResponseEntity<?> deleteNotice(@RequestParam("id") Long noticeId) {
        adminService.deleteNotice(noticeId);
        return ResponseEntity.ok("공지사항 삭제 성공");
    }


//    /**신고 내역 자세히 보기 */
//    @GetMapping("/report/{reportId}")
//    public ResponseEntity<?> getReportDetail(@AuthenticationPrincipal CustomUserDetails user) {
//        return ResponseEntity.ok(adminService.getReportDetail(reportId));
//    }
    /**신고 내역 자세히 보기 */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<AdminReportIdRes> getReportDetail(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long reportId) {
        return ResponseEntity.ok(adminService.getReportDetail(reportId));
    }
//
//    /** 공지·홍보 게시글 작성 */
//    @PostMapping("/board")
//    public ResponseEntity<?> createBoard(@AuthenticationPrincipal CustomUserDetails user) {
//        adminService.createBoard(request);
//        return ResponseEntity.ok("공지/홍보 게시글이 등록되었습니다.");
//    }
//
//    /** 공지·홍보 게시글 수정 */
//    @PostMapping("/board/rectify")
//    public ResponseEntity<?> updateBoard(@AuthenticationPrincipal CustomUserDetails user) {
//        adminService.updateBoard(request);
//        return ResponseEntity.ok("공지/홍보 게시글이 수정되었습니다.");
//    }
//
//    /** 공지·홍보 게시글 삭제 */
//    @DeleteMapping("/board")
//    public ResponseEntity<?> deleteBoard(@AuthenticationPrincipal CustomUserDetails user) {
//        adminService.deleteBoard(boardId);
//        return ResponseEntity.ok("공지/홍보 게시글이 삭제되었습니다.");
//    }
}
