package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkRequestDto;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkResponseDto;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.BookmarkService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     관심 목록에 게시글 추가
     */
    @PostMapping
    public ResponseEntity<String> addBookmark(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody BookmarkRequestDto requestDto
    ) {
        Long userId = user.getUser().getId();
        boolean wasAdded = bookmarkService.addBookmark(userId, requestDto);
        if (wasAdded) {
            return ResponseEntity.ok("관심 목록에 추가되었습니다.");
        } else {
            return ResponseEntity.ok("이미 관심 목록에 있는 게시글입니다.");
        }
    }

    /**
     관심 목록에서 게시글 삭제
     */
    @DeleteMapping
    public ResponseEntity<String> removeBookmark(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody BookmarkRequestDto requestDto
    ) {
        Long userId = user.getUser().getId();
        bookmarkService.removeBookmark(userId, requestDto);
        return ResponseEntity.ok("관심 목록에서 삭제되었습니다.");
    }
    /**
     관심목록조회
    */

    @GetMapping
    public ResponseEntity<List<BookmarkResponseDto>> getMyBookmarks(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("postType") String postType
    ) {
        Long userId = user.getUser().getId();
        List<BookmarkResponseDto> myBookmarks = bookmarkService.getMyBookmarks(userId, postType);
        return ResponseEntity.ok(myBookmarks);
    }
}