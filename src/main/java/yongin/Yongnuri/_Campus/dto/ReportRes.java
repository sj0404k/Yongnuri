package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportRes {
    private String message;
    private Long reporterId;  // 신고자 ID
}
