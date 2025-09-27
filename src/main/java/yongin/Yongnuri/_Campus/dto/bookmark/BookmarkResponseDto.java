package yongin.Yongnuri._Campus.dto.bookmark;

import lombok.Builder;
import lombok.Getter;
import yongin.Yongnuri._Campus.domain.BookMarks;
import yongin.Yongnuri._Campus.domain.Notice;
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


    public static BookmarkResponseDto from(Notice item, String thumbnailUrl, BookMarks bookmark) {
        return BookmarkResponseDto.builder()
                .bookmarkId(bookmark.getId())
                .postId(item.getId())
                .postType("NOTICE") // postType을 "NOTICE"로 설정
                .title(item.getTitle())
                .thumbnailUrl(thumbnailUrl)
                .bookmarkedAt(bookmark.getCreatedAt())
                .build();
    }


}