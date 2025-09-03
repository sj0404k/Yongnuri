package yongin.Yongnuri._Campus.servise;


import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    // 인증번호를 입력해야됨
    public void sendSimpleMailMessage(String email) {
        String trimmedEmail = email.trim();

        if (!trimmedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + trimmedEmail);
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(trimmedEmail);
        simpleMailMessage.setSubject("인증번호 코드");
        simpleMailMessage.setText("테스트 메일 내용");

        try {
            javaMailSender.send(simpleMailMessage);
            log.info("메일 발송 성공!");
        } catch (Exception e) {
            log.error("메일 발송 실패!", e);
            throw new RuntimeException(e);
        }
    }

    public void sendMimeMessage() {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try{
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 메일을 받을 수신자 설정
            mimeMessageHelper.setTo("ksj06030@naver.com");
            // 메일의 제목 설정
            mimeMessageHelper.setSubject("html 적용 테스트 메일 제목");

            // html 문법 적용한 메일의 내용
            String content = """
                    <!DOCTYPE html>
                    <html xmlns:th="http://www.thymeleaf.org">
                                        
                    <body>
                    <div style="margin:100px;">
                        <h1> 인증번호 코드 </h1>
                        <br>
                                        
                                        
                        <div align="center" style="border:1px solid black;">
                            <h3> 테스트 메일 내용 </h3>
                        </div>
                        <br/>
                    </div>
                                        
                    </body>
                    </html>
                    """;

            // 메일의 내용 설정
            mimeMessageHelper.setText(content, true);

            javaMailSender.send(mimeMessage);

            log.info("메일 발송 성공!");
        } catch (Exception e) {
            log.info("메일 발송 실패!");
            throw new RuntimeException(e);
        }
    }

}