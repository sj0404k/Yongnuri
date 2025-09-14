package yongin.Yongnuri._Campus.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MypageRes {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class getpage {
        private String name;
        private String email;
        private String d;
    }
}
