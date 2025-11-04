package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Auth 관련 응답 DTO
 * - 로그인 시: message, accessToken, refreshToken
 * - 이메일 인증 시: emailResDto 내부 클래스 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRes {

    /** ✅ 로그인/회원 관련 공용 응답 */
    private String message;
    private String accessToken;
    private String refreshToken;

    /** ✅ 이메일 인증번호 응답 DTO */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class emailResDto {
        private String checkNum;
    }
}
