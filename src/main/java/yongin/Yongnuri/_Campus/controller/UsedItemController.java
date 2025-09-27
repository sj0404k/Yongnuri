package yongin.Yongnuri._Campus.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid; 
import lombok.RequiredArgsConstructor;
import yongin.Yongnuri._Campus.dto.useditem.UpdateStatusRequestDto;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemCreateRequestDto;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemUpdateRequestDto;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.UsedItemService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/board/market")
public class UsedItemController {

    private final UsedItemService usedItemService;
    private final UserRepository userRepository; 


     // 중고게시판 목록 조회

    @GetMapping
    public ResponseEntity<?> getUsedItems(
            @RequestParam(name = "type", defaultValue = "전체") String type,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<UsedItemResponseDto> items = usedItemService.getUsedItems(user.getUser().getEmail(), type);
        return ResponseEntity.ok(items);
    }


     // 중고게시판 상세 조회

    @GetMapping("/{postId}")
    public ResponseEntity<?> getUsedItemDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UsedItemResponseDto item = usedItemService.getUsedItemDetail(user.getUser().getEmail(), postId);
        return ResponseEntity.ok(item); 
    }

      // 중고거래 게시글 작성

    @PostMapping
    public ResponseEntity<?> createUsedItem(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UsedItemCreateRequestDto requestDto
    ) {
        Long newPostId = usedItemService.createUsedItem(user.getUser().getEmail(), requestDto);

        return ResponseEntity.ok(Map.of(
                "message", "게시글 작성 성공",
                "postId", newPostId
        ));
    }
    //게시글수정
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updateUsedItem(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody UsedItemUpdateRequestDto requestDto
    ) {
        Long updatedPostId = usedItemService.updateUsedItem(user.getUser().getEmail(), postId, requestDto);

        return ResponseEntity.ok(Map.of(
            "message", "게시글 수정 성공",
            "postId", updatedPostId
        ));
    }

    //중고거래 게시글 상태 변경

    @PatchMapping("/{postId}/status")
    public ResponseEntity<String> updateUsedItemStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UpdateStatusRequestDto requestDto
    ) {
        usedItemService.updateUsedItemStatus(user.getUser().getEmail(), postId, requestDto);
        return ResponseEntity.ok("게시글 상태가 변경되었습니다.");
    }
}