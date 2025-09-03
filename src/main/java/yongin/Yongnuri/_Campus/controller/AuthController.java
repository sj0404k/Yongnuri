package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.AuthReq;
import yongin.Yongnuri._Campus.servise.MailService;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final MailService mailService;

    @PostMapping("/checkEmail")
    public void sendSimpleMailMessage(@RequestBody AuthReq.emailReqDto email) {
        mailService.sendSimpleMailMessage(email.getEmail());
    }

    @GetMapping("/html")
    public void sendMimeMessage() {
        mailService.sendMimeMessage();
    }

    // 1. 회원가입
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody AuthReq.joinReqDto req) {

        

        if (Stream.of(req.getEmail(), req.getPassword(), req.getEmailCheck(),
                        req.getPasswordCheck(), req.getName(), req.getMajor(), req.getNickname())
                .anyMatch(Objects::isNull)) {
            return ResponseEntity.badRequest().body("데이터 미입력");
        }
//        if () {
//            return ResponseEntity.status(401).body("비밀번호 불일치");
//        }

        return ResponseEntity.ok("회원가입 성공");
    }

    // 2. 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthReq.loginReqDto req) {
        String email = req.getEmail();
        String password = req.getPassword();

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("데이터 미입력");
        }
        // TODO: DB에서 이메일 조회 -> 없으면 404
        // TODO: 비밀번호 검증 -> 틀리면 401
        // TODO: 성공 시 JWT 토큰 발급

        return ResponseEntity.ok("로그인 성공");
    }

    // 3. 이메일 요청 (인증번호 발송)
    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");

        if (email == null) {
            return ResponseEntity.badRequest().body("이메일 미입력");
        }
        // TODO: 랜덤 번호 생성 + 이메일 발송 + DB/Redis 저장

        return ResponseEntity.ok("이메일 전송 성공");
    }

    // 4. 이메일 확인
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String verificationCode = (String) request.get("verificationCode");

        if (userId == null || verificationCode == null) {
            return ResponseEntity.badRequest().body("데이터 미입력");
        }
        // TODO: 저장된 인증번호와 비교 -> 불일치 시 401

        return ResponseEntity.ok("이메일 인증 성공");
    }

    // 5. 비밀번호 재설정
    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String newPassword = (String) request.get("newPassword");
        String passwordConfirm = (String) request.get("passwordConfirm");
        Boolean emailVerified = (Boolean) request.get("emailVerified");

        if (email == null || newPassword == null || passwordConfirm == null) {
            return ResponseEntity.badRequest().body("데이터 미입력");
        }
        if (!newPassword.equals(passwordConfirm)) {
            return ResponseEntity.status(401).body("비밀번호 불일치");
        }
        if (emailVerified == null || !emailVerified) {
            return ResponseEntity.status(401).body("이메일 인증 필요");
        }
        // TODO: DB에서 비밀번호 업데이트

        return ResponseEntity.ok("비밀번호 재설정 성공");
    }
}
