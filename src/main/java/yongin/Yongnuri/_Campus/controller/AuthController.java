package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.AuthReq;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.repository.VerificationRepository;
import yongin.Yongnuri._Campus.servise.MailService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final MailService mailService;
    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;


    @PostMapping("/checkEmail")
    public void sendSimpleMailMessage(@RequestBody AuthReq.emailReqDto email) {
        mailService.sendSimpleMailMessagetest(email.getEmail());
    }

    @GetMapping("/html")
    public void sendMimeMessage(@RequestBody AuthReq.emailReqDto email) {
        mailService.sendMimeMessage(email.getEmail());
    }

    /*
    1. 회원가입
    이메일 인증, 인증 요청 오면 인증번호 확인 -> 인증시 인증번호 db 저장
    이메일 인증 확인 검증 필요
    이름 학과 닉네임 비번 비번 확인 값 유효성 확인
    비번, 비번 확인 값일치 확인
    회원 정보 저장
     */
//    @PostMapping("/join")
//    public ResponseEntity<?> join(@RequestBody AuthReq.joinReqDto req) {
//
//        // 1. 필수값 확인 (null 또는 빈 문자열 체크)
//        if (Stream.of(req.getEmail(), req.getPassword(), req.getEmailCheck(),
//                        req.getPasswordCheck(), req.getName(), req.getMajor(), req.getNickname())
//                .anyMatch(s -> s == null || s.trim().isEmpty())) {
//            return ResponseEntity.badRequest().body("필수 데이터 미입력");
//        }
//
//        // 2. 이메일 인증 여부 확인 (DB 조회)
//        boolean isEmailVerified = mailService.isVerified(req.getEmail());
//        if (!isEmailVerified) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 인증 실패");
//        }
//
//        // 3. 비밀번호 일치 여부 확인
//        if (!req.getPassword().equals(req.getPasswordCheck())) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호 불일치");
//        }
//
//        // 4. 비밀번호 유효성 검사 (예: 최소 8자, 특수문자 포함)
//        if (!passwordValidator.isValid(req.getPassword())) {
//            return ResponseEntity.badRequest().body("비밀번호 형식 불일치");
//        }
//
//        // 5. 닉네임 중복 확인
//        if (userRepository.existsByNickname(req.getNickname())) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 닉네임");
//        }
//
//        // 6. 회원 정보 저장
//        User newUser = User.builder()
//                .email(req.getEmail())
//                .password(req.getPassword())
//                .name(req.getName())
//                .major(req.getMajor())
//                .nickName(req.getNickname())
//                .build();
//
//        userRepository.save(newUser);
//
//        return ResponseEntity.ok("회원가입 성공");
//    }

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

    // 3. 이메일 요청 (인증번호 발송) 1단 만듬
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody AuthReq.emailReqDto email) {

        if (email == null) {
            return ResponseEntity.badRequest().body("이메일 미입력");
        }
        mailService.sendMimeMessage(email.getEmail());
        return ResponseEntity.ok("이메일 전송 성공");
    }

    // 4. 이메일 확인
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
