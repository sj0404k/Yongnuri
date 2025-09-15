package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class MypageReq {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class setpage {
        private int studentId;
        private String nickName;
        private String email;
    }
}
