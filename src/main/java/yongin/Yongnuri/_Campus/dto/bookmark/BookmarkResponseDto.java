package yongin.Yongnuri._Campus.dto.bookmark;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.*;

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
    private String location;
    private Integer price;
    private LocalDateTime createdAt;
    private String statusBadge;
    @JsonProperty("likeCount")
    private long like;

    public static BookmarkResponseDto from(UsedItem item, String thumbnailUrl, Bookmark bookmark, long likeCount) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("USED_ITEM")
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .location(item.getLocation())
                .price(item.getPrice())
                .createdAt(item.getCreatedAt())
                .statusBadge(item.getStatus().name())
                .like(likeCount)
                .build();
    }

    public static BookmarkResponseDto from(LostItem item, String thumbnailUrl, Bookmark bookmark, long likeCount) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("LOST_ITEM")
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .location(item.getLocation())
                .price(null)
                .createdAt(item.getCreatedAt())
                .statusBadge(item.getStatus().name())
                .like(likeCount)
                .build();
    }
    public static BookmarkResponseDto from(GroupBuy item, String thumbnailUrl, Bookmark bookmark, long likeCount) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("GROUP_BUY")
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .location(null)
                .price(null)
                .createdAt(item.getCreatedAt())
                .statusBadge(item.getStatus().name())
                .like(likeCount)
                .build();
    }
    public static BookmarkResponseDto from(Notice item, String thumbnailUrl, Bookmark bookmark, long likeCount) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("NOTICE")
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .location(null)
                .price(null)
                .createdAt(item.getCreatedAt())
                .statusBadge(item.getStatus().name())
                .like(likeCount)
                .build();
    }
}