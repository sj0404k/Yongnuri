package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SearchBoard {
    private Long id;
    private String title;
    private String location;
    private Integer price;
    private String createdAt;
    private long like;
    private String boardType;

    private String thumbnailUrl;
}
