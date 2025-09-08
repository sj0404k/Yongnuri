package yongin.Yongnuri._Campus.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String email;
    @Column
    private int code;
    //인증 여부 확인
    @Column
    private boolean verified;
    //인증 횟수 카운트
    @Column
    private int count;
    @Column
    private LocalDateTime createdAt;

    // 인증번호 유효 여부 계산
    public boolean isExpired() {
        return createdAt.plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
