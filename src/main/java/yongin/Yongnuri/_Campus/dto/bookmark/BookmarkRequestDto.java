package yongin.Yongnuri._Campus.dto.bookmark;

import lombok.Getter;

@Getter
public class BookmarkRequestDto {
    private String postType; // "USED_ITEM,LOST_ITEM"
    private Long postId;
}