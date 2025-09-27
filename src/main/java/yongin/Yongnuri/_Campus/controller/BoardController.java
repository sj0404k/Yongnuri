package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.BoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    @DeleteMapping("/delete-post")
    public ResponseEntity<String> deletePost(
            @RequestParam("postType") String postType,
            @RequestParam("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        boardService.deletePost(user.getUser().getEmail(), postType, postId);
        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
    }
}