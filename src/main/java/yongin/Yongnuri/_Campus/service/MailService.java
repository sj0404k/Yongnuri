package yongin.Yongnuri._Campus.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 처리를 위해 추가
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

    private static final int VERIFICATION_CODE_LENGTH = 5; // 인증 코드 길이
    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 10; // Verification 엔티티의 isExpired() 사용
    private static final int MAX_VERIFICATION_ATTEMPTS = 5; // 최대 인증 시도 횟수

    // 이메일 인증 번호 전송
    @Transactional // DB 저장 및 업데이트는 하나의 트랜잭션으로 묶습니다.
    public void sendMimeMessage(String email) {

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + email);
        }

        // 1. 랜덤 5자리 숫자 인증 코드 생성
        SecureRandom random = new SecureRandom();
        // 10000 ~ 99999 범위 (VERIFICATION_CODE_LENGTH가 5인 경우)
        int code = random.nextInt((int) Math.pow(10, VERIFICATION_CODE_LENGTH) - (int) Math.pow(10, VERIFICATION_CODE_LENGTH - 1))
                + (int) Math.pow(10, VERIFICATION_CODE_LENGTH - 1);

        // 2. 기존 인증 코드 처리 (만약 이메일당 하나의 활성 코드만 존재하도록 하려면 findByEmail 사용하는 것이 더 좋습니다.)
        // 현재 Repository의 findTopByEmailOrderByCreatedAtDesc 메서드를 사용하므로,
        // 해당 이메일의 가장 최근 발송 코드를 찾습니다.
        Optional<Verification> optionalExistingVerification = verificationRepository.findTopByEmailOrderByCreatedAtDesc(email);

        Verification verification;
        if (optionalExistingVerification.isPresent()) {
            // 이미 존재하는 이메일에 대한 인증 요청인 경우: 기존 레코드 업데이트
            verification = optionalExistingVerification.get();
            verification.updateCodeAndReset(code); // 엔티티의 updateCodeAndReset 메서드 호출
        } else {
            // 새로운 인증 요청인 경우: 새 레코드 생성
            verification = Verification.builder()
                    .email(email)
                    .code(code)
                    .createdAt(LocalDateTime.now())
                    .verified(false) // 초기에는 미인증 상태
                    .count(0) // 시도 횟수 초기화
                    .build();
        }

        try {
            // 3. 이메일 발송
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Yongnuri Campus 이메일 인증 코드");

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

            // 4. DB 저장 또는 업데이트
            // 이미 Optional에서 가져온 엔티티는 save하면 dirty checking에 의해 업데이트됩니다.
            // 새로 만든 엔티티는 저장됩니다.
            verificationRepository.save(verification);

            log.info("메일 발송 성공! 이메일: {}, 인증번호: {}", email, code);

        } catch (Exception e) {
            log.error("메일 발송 실패! 이메일: {}, 오류: {}", email, e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 다시 시도해주세요. ㅠㅠ");
        }
    }

    // 이메일 인증 코드 확인 (인증 시도)
    @Transactional // DB 업데이트를 위해 트랜잭션 필요
    public boolean verifyCode(String email, int inputCode) {
        // 이메일에 대한 가장 최근 발급된 인증 코드를 찾습니다.
        Optional<Verification> optionalVerification = verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email);

        if (optionalVerification.isEmpty()) {
            log.warn("인증을 시도했지만 해당 이메일({})에 대한 인증 코드를 찾을 수 없습니다.", email);
            return false; // 인증 코드를 찾을 수 없음 (요청한 적 없거나, 너무 오래되서 삭제됨 등)
        }

        Verification v = optionalVerification.get();

        // 1. 이미 인증된 코드인지 확인 (한번 사용된 코드는 다시 인증될 수 없도록)
        if (v.isVerified()) {
            log.warn("이메일({})에 대한 인증 코드가 이미 인증되었습니다. (재시도: {})", email, inputCode);
            // 이미 인증된 코드가 발견되었으므로, 새롭게 인증할 필요 없음 (성공으로 간주할 수도 있음)
            return false; // 또는 true로 반환하여 이미 완료된 상태임을 알릴 수도 있습니다.
        }

        // 2. 인증 시도 횟수 초과 확인
        if (v.getCount() >= MAX_VERIFICATION_ATTEMPTS) {
            log.warn("이메일({})에 대한 인증 시도 횟수를 초과했습니다. 코드: {}, 시도 횟수: {}", email, v.getCode(), v.getCount());
            // 시도 횟수 초과 시 해당 코드를 더 이상 유효하지 않도록 처리하는 것이 일반적입니다.
            // 예를 들어 DB에서 삭제하거나, 만료 처리할 수 있습니다.
            // verificationRepository.delete(v);
            return false;
        }

        // 3. 인증 코드 만료 여부 확인 (10분 이상 지난 코드)
        if (v.isExpired()) {
            log.warn("이메일({})에 대한 인증 코드가 만료되었습니다. 코드: {}", email, v.getCode());
            // 만료된 코드는 더 이상 사용할 수 없으므로 실패 처리.
            // 만료된 코드를 DB에서 삭제하여 불필요한 데이터를 정리할 수도 있습니다.
            // verificationRepository.delete(v);
            return false;
        }

        // 4. 입력 코드와 저장된 코드 일치 여부 확인
        if (v.getCode() == inputCode) {
            // 5. 인증 성공 처리
            v.markAsVerified(); // 엔티티의 markAsVerified() 메서드 호출
            verificationRepository.save(v); // DB에 변경 사항 반영
            log.info("이메일({}) 인증 성공! 코드: {}", email, inputCode);
            return true;
        } else {
            // 6. 인증 실패 처리: 시도 횟수 증가
            v.incrementCount(); // 엔티티의 incrementCount() 메서드 호출
            verificationRepository.save(v); // DB에 변경 사항 반영
            log.warn("이메일({}) 인증 실패! 입력 코드: {}, 저장된 코드: {}, 시도 횟수: {}", email, inputCode, v.getCode(), v.getCount());
            return false;
        }
    }

    // 이메일 인증이 최종적으로 완료되었는지 확인 (회원 가입 등의 최종 단계에서 사용)
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public boolean isEmailVerified(String email) {
        // 가장 최근에 발송된 코드를 기준으로 인증 여부를 확인합니다.
        Optional<Verification> optionalVerification = verificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email);

        // 코드가 존재하고, 그 코드가 'verified' 상태인 경우에만 true 반환
        return optionalVerification.map(Verification::isVerified).orElse(false);
    }


    // (옵션) 회원 가입 완료 후 인증 코드를 삭제하는 메서드
    @Transactional
    public void deleteVerifiedCode(String email) {
        verificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .ifPresent(verificationRepository::delete);
        log.info("이메일({})에 대한 인증 코드가 삭제되었습니다.", email);
    }
}