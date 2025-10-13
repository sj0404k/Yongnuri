package yongin.Yongnuri._Campus.dto.lostitem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.Image;

@Getter
public class LostItemResponseDto {
    private final Long id;
    private final String title;
    private final String location;
    private final String purpose;
    private final String status;
    private final String content;
    private final LocalDateTime createdAt;
    private final String authorNickname;
    private final List<ImageDto> images;
    private final Enum.authStatus authorStatus;
    private String authorDepartment;        // 작성자 학과(=User.major)
    private String authorEmail;             // ✅ 오너 판정용
    @Setter
    private String thumbnailUrl;
    @Setter
    private boolean isBookmarked;
    @Setter
    private long bookmarkCount;

    public LostItemResponseDto(LostItem item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.content = null;
        this.location = item.getLocation();
        this.purpose = item.getPurpose().name();
        this.status = item.getStatus().name();

        this.createdAt = item.getCreatedAt();
        this.authorNickname = null;
        this.images = null;
        this.isBookmarked = false;
        this.bookmarkCount = 0;
        this.authorStatus = null;

    }

    public LostItemResponseDto(LostItem item, List<Image> images) {
        this.authorEmail = item.getUser().getEmail();
        this.authorDepartment = item.getUser().getMajor();
        this.id = item.getId();
        this.title = item.getTitle();
        this.content = item.getContent();
        this.location = item.getLocation();
        this.purpose = item.getPurpose().name();
        this.status = item.getStatus().name();
        this.createdAt = item.getCreatedAt();
        this.authorNickname = item.getUser().getNickName();
        this.images = images.stream().map(ImageDto::new).collect(Collectors.toList());
        this.isBookmarked = false;
        this.bookmarkCount = 0;
        this.authorStatus = item.getUser().getStatus();
    }
}