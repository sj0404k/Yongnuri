package yongin.Yongnuri._Campus.servise;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.Verification;
import yongin.Yongnuri._Campus.repository.VerificationRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final VerificationRepository verificationRepository;
    private final JavaMailSender javaMailSender;

    //이메일 인증 번호 전송
    public void sendMimeMessage(String email) {

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + email);
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // 랜덤 5자리 숫자 생성
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(90000) + 10000; // 10000~99999 범위

        try{
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 메일을 받을 수신자 설정
            mimeMessageHelper.setTo(email);
            // 메일의 제목 설정
            mimeMessageHelper.setSubject("Yongnuri Compus 이메일 인증 코드");

            // HTML 내용에 인증번호 삽입
            String content = String.format("""
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <body>
                    <div style="margin:100px; text-align:center;">
                        <h1 style="font-size: 32px; margin-bottom: 20px;">인증번호</h1>
                        <div style="
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            border: 2px solid black;
                            width: 200px;
                            height: 120px;
                            margin: 0 auto;
                            border-radius: 10px;
                        ">
                            <span style="font-size: 48px; font-weight: bold;">%d</span>
                        </div>
                    </div>
                </body>
                </html>
                """, code);
            mimeMessageHelper.setText(content, true);
            javaMailSender.send(mimeMessage);

            //DB 저장
            Verification verification = Verification.builder()
                    .email(email)
                    .code(code)
                    .count(+1)
                    .createdAt(LocalDateTime.now())
                    .verified(false)
                    .build();

            verificationRepository.save(verification);

            log.info("메일 발송 성공! (인증번호: {})", code);


        } catch (Exception e) {
            log.info("메일 발송 실패!");
            throw new RuntimeException(e);
        }
    }
    // 이해일 인증 놀었을때
    public boolean verifyCode(String email, int inputCode) {
        Optional<Verification> optionalVerification = verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email);

        if (optionalVerification.isPresent()) {
            Verification v = optionalVerification.get();

            // 10분 이상 지난 인증번호는 만료
            if (!v.isExpired() && v.getCode() == inputCode) {
                v.setVerified(true);
                verificationRepository.save(v);
                return true;
            }
        }

        return false;
    }

    //이메일 인증했는지 확인
    public boolean isVerified(String email) {
        return verificationRepository.findByEmail(email)
                .map(Verification::isVerified)  // DB에 저장된 isVerified 필드 확인
                .orElse(false); // 데이터 없으면 인증 실패로 간주
    }


}