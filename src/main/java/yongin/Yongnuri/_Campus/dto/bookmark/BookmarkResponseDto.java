package yongin.Yongnuri._Campus.dto.bookmark;

import lombok.Builder;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.Bookmark;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.GroupBuy;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponseDto {
    private Long bookmarkId;
    private Long postId;
    private String postType;
    private String title;
    private String thumbnailUrl;
    private LocalDateTime bookmarkedAt;

    public static BookmarkResponseDto from(UsedItem item, String thumbnailUrl, Bookmark bookmark) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("USED_ITEM")
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .build();
    }

    public static BookmarkResponseDto from(LostItem item, String thumbnailUrl, Bookmark bookmark) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("LOST_ITEM")
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .build();
    }
    public static BookmarkResponseDto from(GroupBuy item, String thumbnailUrl, Bookmark bookmark) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("GROUP_BUY")
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .build();
    }
}