package yongin.Yongnuri._Campus.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthReq {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class joinReqDto {
        private String email;
        private String name;
        private String major;
        private String nickname;
        private String password;
        private String passwordCheck;
//        private String emailCheck;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class loginReqDto {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class emailReqDto {
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class verifyReqDto {
        private String email;
        private int number;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class resetPasswordReqDto {
        private String email;
//        private String emailCheck;
        private String password;
        private String passwordCheck;
    }
}
