package yongin.Yongnuri._Campus.dto;

import lombok.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId; // 개별 사용자용
    private List<Long> targetUserIds; // 여러 사용자용
    private boolean targetAll; // 전체 사용자용
    private String title;
    private String message;

}
