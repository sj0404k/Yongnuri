package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.BlocksRes;
import yongin.Yongnuri._Campus.dto.MypageReq;
import yongin.Yongnuri._Campus.dto.MypageRes;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.security.JwtProvider;
import yongin.Yongnuri._Campus.service.MailService;
import yongin.Yongnuri._Campus.service.MypageService;

import java.util.List;

@RestController
@RequestMapping("/mypage")
@AllArgsConstructor
public class MypageController {
    private final MypageService mypageService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final MailService mailService;

    @GetMapping
    public ResponseEntity<MypageRes.getpage> getMyPageInfo(@AuthenticationPrincipal CustomUserDetails user) {

        MypageRes.getpage mypageInfo = mypageService.getMypageDetails(user.getUser().getEmail());
        return ResponseEntity.ok(mypageInfo);
    }
    @PostMapping
    public ResponseEntity<?> setMyPageInfo(@AuthenticationPrincipal CustomUserDetails user,@RequestBody MypageReq.setpage mypageReq) {
//        String email = user.getUsername();
        mypageService.setMypageDetails(user.getUser().getEmail(),mypageReq);

        return ResponseEntity.ok("내 정보 수정 완료");
    }
    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<String> deleteBookMarks(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("bookmarkId") Long bookmarkId) {
        boolean deleted = mypageService.deleteBookMarks(user.getUser().getEmail(),bookmarkId);

        if (deleted) {
            return ResponseEntity.ok("북마크 취소 성공");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("해당 북마크를 찾을 수 없습니다.");
        }
    }
    @PostMapping("/blocks")
    public ResponseEntity<?> postBlock(@AuthenticationPrincipal CustomUserDetails user,@RequestBody MypageReq.blocks mypageReq) {
        boolean blocked = mypageService.postBlocks(user.getUser().getEmail(), mypageReq);
        if (blocked) {
            return ResponseEntity.status(HttpStatus.CREATED).body("차단 성공");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 차단한 유저입니다.");
        }
    }
    @GetMapping("/blocks")
    public ResponseEntity<List<BlocksRes>> getBlocks(
            @AuthenticationPrincipal CustomUserDetails user) {

        List<BlocksRes> blocks = mypageService.getBlocks(user.getUser().getEmail());

        return ResponseEntity.ok(blocks); // 200 OK + JSON 배열 반환
    }
    @DeleteMapping("/blocks/{blockedId}")
    public ResponseEntity<?> deleteBlock(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("blockedId") Long blockedId) {
        boolean deleted = mypageService.deleteBlocks(user.getUser().getEmail(), blockedId);

        if (deleted) {
            return ResponseEntity.ok("차단한 유저 취소 완료");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("차단 내역을 찾을 수 없습니다.");
        }

//        if (deleted) {
//            return ResponseEntity.noContent().build(); // 204 No Content
//        } else {
//            return ResponseEntity.notFound().build();  // 404 Not Found
//        }
    }
}
