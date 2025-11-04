package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.AuthReq;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.repository.VerificationRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.AuthService;
import yongin.Yongnuri._Campus.service.MailService;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final MailService mailService;
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final AuthService authService;

    @PostMapping("/join") // 블랙 당한 사람은 재가입 막기(서비스에서 처리)
    public ResponseEntity<String> join(@RequestBody AuthReq.joinReqDto req) {
        authService.join(req);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthReq.loginReqDto req) {
        return authService.login(req.getEmail(), req.getPassword());
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody AuthReq.emailReqDto email) {
        if (email == null) {
            return ResponseEntity.badRequest().body("이메일 미입력");
        }
        mailService.sendMimeMessage(email.getEmail());
        return ResponseEntity.ok("이메일 전송 성공");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody AuthReq.verifyReqDto req) {
        Integer number = req.getNumber();
        if (req.getEmail() == null || number == null) {
            return ResponseEntity.badRequest().body("데이터 미입력");
        }
        boolean verified = mailService.verifyCode(req.getEmail(), req.getNumber());

        if (verified) {
            return ResponseEntity.ok("이메일 인증 성공");
        } else {
            return ResponseEntity.badRequest().body("인증번호가 일치하지 않거나 만료되었습니다.");
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody AuthReq.resetPasswordReqDto req) {
        authService.rePassword(req);
        return ResponseEntity.ok("비밀번호 재설정 성공");
    }

    /**
     * 계정 탈퇴 (인증 필요)
     */
    @PostMapping("/deleteAccount")
    public void deleteAccount(@AuthenticationPrincipal CustomUserDetails user){
        authService.deleteAccount(user);
        log.info("회원 id 값: {}, 현재 상태값: {}", user.getUser().getId(), user.getUser().getStatus());
    }
}
