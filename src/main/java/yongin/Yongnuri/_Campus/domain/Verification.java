package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 사용 시 PROTECTED 기본 생성자 권장
@AllArgsConstructor
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // 이메일에 @Unique 를 추가하는 것을 고려해보세요.
    private String email;

    @Column(nullable = false)
    private int code;

    //인증 여부 확인
    @Column(nullable = false)
    private boolean verified;

    // 인증 시도 횟수 카운트 (이름을 attempts로 변경하는 것을 고려할 수 있습니다.)
    @Column(nullable = false)
    private int count;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 인증번호 유효 여부 계산
    public boolean isExpired() {
        return createdAt.plusMinutes(10).isBefore(LocalDateTime.now());
    }

    // --- 추가 제안 메서드들 ---

    // 1. 인증 성공 시 호출할 메서드
    public void markAsVerified() {
        this.verified = true;
        // this.verifiedAt = LocalDateTime.now(); // 만약 인증 완료 시간을 기록하고 싶다면 필드 추가 후 사용
    }

    // 2. 인증 시도 횟수 증가 메서드
    public void incrementCount() {
        this.count++;
    }

    // 3. 새 코드를 발급받았을 때 초기화 및 업데이트 메서드
    // 이전 코드는 무효화되고 새 코드가 부여될 때 사용
    public void updateCodeAndReset(int newCode) {
        this.code = newCode;
        this.createdAt = LocalDateTime.now();
        this.verified = false; // 새 코드는 다시 미인증 상태
        this.count = 0; // 새 코드에 대한 시도 횟수 초기화
    }
}