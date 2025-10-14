package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportRes {
    private String message;
    private Long reportedUserId;  // 신고받은사람 ID
}
