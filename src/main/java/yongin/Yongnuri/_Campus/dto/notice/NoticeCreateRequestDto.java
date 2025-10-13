package yongin.Yongnuri._Campus.dto.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NoticeCreateRequestDto {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    @NotBlank(message = "상태는 필수입니다.")
    private String status;
    private String link;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> imageUrls;
}