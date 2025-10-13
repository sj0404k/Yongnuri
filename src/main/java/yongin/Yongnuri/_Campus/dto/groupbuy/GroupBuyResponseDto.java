package yongin.Yongnuri._Campus.dto.groupbuy;

import lombok.Getter;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.GroupBuy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import yongin.Yongnuri._Campus.domain.Image;
import yongin.Yongnuri._Campus.domain.User;

@Getter
public class GroupBuyResponseDto {

    private final Long id;
    private final String title;
    private final String status;
    private final Integer limit;
    private final LocalDateTime createdAt;

    private final String content;
    private final String link;
    private final String authorNickname;
    private final List<ImageDto> images;
    private final Enum.authStatus authorStatus;

    private String authorDepartment;
    private String authorEmail;

    @Setter
    private Long currentCount;
    @Setter
    private String thumbnailUrl;
    @Setter
    private boolean isBookmarked;
    @Setter
    private long bookmarkCount;
    // 공동구매목록 조회
    public GroupBuyResponseDto(GroupBuy item) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.status = item.getStatus().name();
        this.limit = item.getLimit();
        this.createdAt = item.getCreatedAt();
        this.content = null;
        this.link = null;
        this.authorNickname = null;
        this.images = null;
        this.authorStatus=null;
        this.isBookmarked = false;
        this.bookmarkCount = 0L;
        this.currentCount = (item.getCurrentCount() == null) ? 0L : item.getCurrentCount().longValue();
    }

    // 공동구매상세 조회
    public GroupBuyResponseDto(GroupBuy item, User author, List<Image> images) {
        this.authorEmail = author.getEmail();
        this.authorDepartment = author.getMajor();
        this.id = item.getId();
        this.title = item.getTitle();
        this.status = item.getStatus().name();
        this.limit = item.getLimit();
        this.createdAt = item.getCreatedAt();
        this.content = item.getContent();
        this.link = item.getLink();
        this.isBookmarked = false;
        this.bookmarkCount = 0L;
        this.currentCount = (item.getCurrentCount() == null) ? 0L : item.getCurrentCount().longValue();
        this.authorNickname = author.getNickName();
        this.authorStatus= Objects.requireNonNull(author).getStatus();
        this.images = images.stream().map(ImageDto::new).collect(Collectors.toList());
    }


}