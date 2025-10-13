package yongin.Yongnuri._Campus.dto.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserInfoRes {
    private Long id;
    private String name;
    private String userNickname;
    private int studentId;
    private String major;
    private Long reportCount;
}
