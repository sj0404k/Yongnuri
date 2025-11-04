package yongin.Yongnuri._Campus.dto.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllNoticeReq {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
