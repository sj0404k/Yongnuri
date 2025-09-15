package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.AuthReq;
import yongin.Yongnuri._Campus.dto.MypageReq;
import yongin.Yongnuri._Campus.dto.MypageRes;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.security.JwtProvider;
import yongin.Yongnuri._Campus.servise.AuthService;
import yongin.Yongnuri._Campus.servise.MailService;
import yongin.Yongnuri._Campus.servise.MypageService;

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
//        String email = "202033008@yiu.ac.kr";
        String email = user.getUsername();
 //       String email = jwtProvider.getEmailFromToken(token);
        MypageRes.getpage mypageInfo = mypageService.getMypageDetails(email);
        return ResponseEntity.ok(mypageInfo);
    }
    @PostMapping
    public ResponseEntity<MypageReq.setpage> setMyPageInfo(@AuthenticationPrincipal CustomUserDetails user, MypageReq.setpage mypageReq) {
        String email = user.getUsername();

        MypageReq.setpage mypageInfo = mypageService.setMypageDetails(email,mypageReq);

        return ResponseEntity.ok(mypageInfo);
    }
    @GetMapping("/bookmarks")
    public ResponseEntity<?> bookMarks(@AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok("test");
    }
}
