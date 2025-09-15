package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.AuthReq;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.repository.VerificationRepository;
import yongin.Yongnuri._Campus.servise.AuthService;
import yongin.Yongnuri._Campus.servise.MailService;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final MailService mailService;
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final AuthService authService;
    /*
    회원가입
    이메일 인증, 인증 요청 오면 인증번호 확인 -> 인증시 인증번호 db 저장
    이메일 인증 확인 검증 필요
    이름 학과 닉네임 비번 비번 확인 값 유효성 확인
    비번, 비번 확인 값일치 확인
    회원 정보 저장
    그냥 인증 된거 다 검증하고 db에 저장
     */
    // 1. 회원가입 1단 됬나?
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody AuthReq.joinReqDto req) {
        authService.join(req);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 2. 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthReq.loginReqDto req) {

       return authService.login(req.getEmail(),req.getPassword());
//        return ResponseEntity.ok("로그인 성공");
    }

    // 3. 이메일 요청 (인증번호 발송) 1단 만듬
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody AuthReq.emailReqDto email) {

        if (email == null) {
            return ResponseEntity.badRequest().body("이메일 미입력");
        }
        mailService.sendMimeMessage(email.getEmail());
        return ResponseEntity.ok("이메일 전송 성공");
    }

    // 4. 이메일 확인 1단 만듬
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

    // 5. 비밀번호 재설정 -- 아직 안함 오류가 좀 있어서 후순위로 만들 예정
    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody AuthReq.resetPasswordReqDto req) {

//        authService.rePassword(req);
        return ResponseEntity.ok("비밀번호 재설정 성공");
    }
}
