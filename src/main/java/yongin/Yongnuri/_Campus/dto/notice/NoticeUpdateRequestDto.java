package yongin.Yongnuri._Campus.dto.notice;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NoticeUpdateRequestDto {
    private String title;
    private String content;
    private String status;
    private String link;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> imageUrls;
}