package yongin.Yongnuri._Campus.dto;

import lombok.Getter;
import lombok.Setter;


public class AuthReq {
    @Getter
    @Setter
    public class joinReqDto{
        private String email;
        private String name;
        private String emailCheck;
        private String major;
        private String nickname;
        private String password;
        private String passwordCheck;
    }
    @Getter
    @Setter
    public class loginReqDto{
        private String email;
        private String password;
    }
    @Getter
    @Setter
    public class emailReqDto{
        private String email;
    }
    @Getter
    @Setter
    public class verifyReqDto{
        private String numberCheck;
        private String numberId;
    }
    @Getter
    @Setter
    public class resetPasswordReqDto{
        private String email;
        private String emailCheck;
        private String password;
        private String passwordCheck;
    }


}
