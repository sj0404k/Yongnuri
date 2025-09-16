// controller/UsedItemController.java
package yongin.Yongnuri._Campus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // (추가)
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable; // (추가)
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // (수정) PostMapping, RequestBody 임포트
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid; // (추가)
import lombok.RequiredArgsConstructor;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemCreateRequestDto;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemUpdateRequestDto;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.servise.UsedItemService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/used-items") // (선택) 공통 경로를 클래스 레벨로 올리는 것을 추천
public class UsedItemController {

    private final UsedItemService usedItemService;
    private final UserRepository userRepository; 

    /**
     * GET /used-items (목록 조회)
     */
    @GetMapping
    public ResponseEntity<?> getUsedItems(
            @RequestParam(name = "type", defaultValue = "전체") String type,
            @AuthenticationPrincipal String email
    ) {
        List<UsedItemResponseDto> items = usedItemService.getUsedItems(email, type); 
        return ResponseEntity.ok(items);
    }

    /**
     * GET /used-items/{postId} (상세 조회)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getUsedItemDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal String email
    ) {
        UsedItemResponseDto item = usedItemService.getUsedItemDetail(email, postId);
        return ResponseEntity.ok(item); 
    }

    /**
     * (추가) POST /used-items (중고거래 게시글 작성)
     */
    @PostMapping
    public ResponseEntity<?> createUsedItem(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UsedItemCreateRequestDto requestDto 
    ) {
        Long newPostId = usedItemService.createUsedItem(email, requestDto);

        // 200: 성공 (생성된 게시글 ID 응답)
        return ResponseEntity.ok(Map.of(
            "message", "게시글 작성 성공",
            "postId", newPostId
        ));
    }
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updateUsedItem(
            @PathVariable Long postId,
            @AuthenticationPrincipal String email,
            @RequestBody UsedItemUpdateRequestDto requestDto // (수정 DTO)
    ) {
        Long updatedPostId = usedItemService.updateUsedItem(email, postId, requestDto);

        // (200: 성공) 수정된 게시글 ID를 응답
        return ResponseEntity.ok(Map.of(
            "message", "게시글 수정 성공",
            "postId", updatedPostId
        ));
    }
}