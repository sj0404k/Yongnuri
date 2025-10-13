package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;
@Getter
@Setter
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;
    private String token;
    private String title;
    private String message;

}
