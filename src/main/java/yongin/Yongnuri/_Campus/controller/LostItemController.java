package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import yongin.Yongnuri._Campus.dto.lostitem.LostItemResponseDto;
import yongin.Yongnuri._Campus.servise.LostItemService;
import java.util.List;
import java.util.Map; 
import jakarta.validation.Valid; 
import org.springframework.web.bind.annotation.*; 
import yongin.Yongnuri._Campus.dto.lostitem.LostItemCreateRequestDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemUpdateRequestDto; 


@RestController
@RequiredArgsConstructor
@RequestMapping("/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;

    /*
     GET /lost-items (분실물 목록 조회)
    */
    @GetMapping
    public ResponseEntity<?> getLostItems(
            @RequestParam(name = "type", defaultValue = "전체") String type,
            @AuthenticationPrincipal String email
    ) {
        List<LostItemResponseDto> items = lostItemService.getLostItems(email, type);
        return ResponseEntity.ok(items);
    }
    /*
     * GET /lost-items/{postId} (분실물 상세 조회)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getLostItemDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal String email
    ) {
        LostItemResponseDto item = lostItemService.getLostItemDetail(email, postId);
        return ResponseEntity.ok(item); 
    }
    /*
      POST /lost-items (분실물 게시글 작성)
     */
    @PostMapping
    public ResponseEntity<?> createLostItem(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody LostItemCreateRequestDto requestDto
    ) {
        Long newPostId = lostItemService.createLostItem(email, requestDto);
        return ResponseEntity.ok(Map.of(
            "message", "게시글 작성 성공",
            "postId", newPostId
        ));
    }
    /*
      PATCH /lost-items/{postId} (분실물 게시글 수정)
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updateLostItem(
            @PathVariable Long postId,
            @AuthenticationPrincipal String email,
            @RequestBody LostItemUpdateRequestDto requestDto
    ) {
        Long updatedPostId = lostItemService.updateLostItem(email, postId, requestDto);


        return ResponseEntity.ok(Map.of(
            "message", "게시글 수정 성공",
            "postId", updatedPostId
        ));
    }
}