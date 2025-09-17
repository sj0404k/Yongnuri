package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private int code;
    //인증 여부 확인
    private boolean verified;
    private int count;
    private LocalDateTime createdAt;

    // 인증번호 유효 여부 계산
    public boolean isExpired() {
        return createdAt.plusMinutes(10).isBefore(LocalDateTime.now());
    }
    }

    // 3. 새 코드를 발급받았을 때 초기화 및 업데이트 메서드
    // 이전 코드는 무효화되고 새 코드가 부여될 때 사용
//    public void updateCodeAndReset(int newCode) {
//        this.code = newCode;
//        this.createdAt = LocalDateTime.now();
//        this.verified = false; // 새 코드는 다시 미인증 상태
//        this.count = 0; // 새 코드에 대한 시도 횟수 초기화
//    }

