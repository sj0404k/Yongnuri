// yongin/Yongnuri/_Campus/dto/MypageRes.java
package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MypageRes {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class getpage {
        private String studentId; // ✅ 학번은 문자열로 내려서 선행 0 손실 방지
        private String name;
        private String email;
        private String nickName;  // ✅ 닉네임 추가
        // 필요 시 major 등 필드 확장 가능
    }
}
