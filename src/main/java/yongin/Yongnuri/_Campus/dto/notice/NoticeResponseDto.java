package yongin.Yongnuri._Campus.dto.notice;

import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.Notice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class NoticeResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final String link;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final LocalDateTime createdAt;
    private final String authorNickname;
    private final List<ImageDto> images;

    @Setter
    private String thumbnailUrl;
    @Setter
    private boolean isBookmarked;

    // 목록 조회
    public NoticeResponseDto(Notice item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.link = item.getLink();
        this.startDate = item.getStartDate();
        this.endDate = item.getEndDate();
        this.createdAt = item.getCreatedAt();
        this.authorNickname = item.getAuthor().getNickName();
        // 목록에서는 content와 images 제외
        this.content = null;
        this.images = null;
    }

    // 상세 조회
    public NoticeResponseDto(Notice item, List<Image> images) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent();
        this.link = item.getLink();
        this.startDate = item.getStartDate();
        this.endDate = item.getEndDate();
        this.createdAt = item.getCreatedAt();
        this.authorNickname = item.getAuthor().getNickName();
        this.images = images.stream().map(ImageDto::new).collect(Collectors.toList());
    }
}