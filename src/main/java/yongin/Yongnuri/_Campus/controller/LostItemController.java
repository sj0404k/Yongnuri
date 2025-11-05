package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemResponseDto;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.LostItemService;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemCreateRequestDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemUpdateRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board/lost-found")
public class LostItemController {

    private final LostItemService lostItemService;

    @GetMapping
    public ResponseEntity<?> getLostItems(
            @RequestParam(name = "type", defaultValue = "전체") String type,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<LostItemResponseDto> items = lostItemService.getLostItems(user.getUser().getEmail(), type);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getLostItemDetail(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        LostItemResponseDto item = lostItemService.getLostItemDetail(user.getUser().getEmail(), postId);
        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<?> createLostItem(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody LostItemCreateRequestDto requestDto
    ) {
        Long newPostId = lostItemService.createLostItem(user.getUser().getEmail(), requestDto);
        return ResponseEntity.ok(Map.of("message", "게시글 작성 성공", "postId", newPostId));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> updateLostItem(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody LostItemUpdateRequestDto requestDto
    ) {
        Long updatedPostId = lostItemService.updateLostItem(user.getUser().getEmail(), postId, requestDto);
        return ResponseEntity.ok(Map.of("message", "게시글 수정 성공", "postId", updatedPostId));
    }
}
