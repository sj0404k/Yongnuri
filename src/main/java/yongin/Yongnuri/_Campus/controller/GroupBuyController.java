package yongin.Yongnuri._Campus.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyCreateRequestDto;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyResponseDto;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyUpdateRequestDto;
import yongin.Yongnuri._Campus.dto.groupbuy.UpdateCountRequestDto;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.GroupBuyService;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board/group-buys")
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
//공동구매글목록
    @GetMapping
    public ResponseEntity<List<GroupBuyResponseDto>> getGroupBuys(
            @RequestParam(name = "type", defaultValue = "전체") String type,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<GroupBuyResponseDto> items = groupBuyService.getGroupBuys(user.getUser().getEmail(), type);
        return ResponseEntity.ok(items);
    }
    //공동구매글상세보기
    @GetMapping("/{postId}")
    public ResponseEntity<GroupBuyResponseDto> getGroupBuyDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        GroupBuyResponseDto item = groupBuyService.getGroupBuyDetail(user.getUser().getEmail(), postId);
        return ResponseEntity.ok(item);
    }
  //공동구매글작성
    @PostMapping
    public ResponseEntity<?> createGroupBuy(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody GroupBuyCreateRequestDto requestDto
    ) {
        Long newPostId = groupBuyService.createGroupBuy(user.getUser().getEmail(), requestDto);
        return ResponseEntity.ok(Map.of("message", "게시글 작성 성공", "postId", newPostId));
    }

    //공동구매글수정
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updateGroupBuy(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody GroupBuyUpdateRequestDto requestDto
    ) {
        Long updatedPostId = groupBuyService.updateGroupBuy(user.getUser().getEmail(), postId, requestDto);
        return ResponseEntity.ok(Map.of("message", "게시글 수정 성공", "postId", updatedPostId));
    }

     //공동구매 신청
    @PostMapping("/{postId}/apply")
    public ResponseEntity<String> applyToGroupBuy(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        groupBuyService.applyForGroupBuy(user.getUser().getEmail(), postId);
        return ResponseEntity.ok("공동구매 신청이 완료되었습니다.");
    }

     //현재 모집 인원 수정
     @PatchMapping("/{postId}/current-count")
     public ResponseEntity<GroupBuyResponseDto> updateCurrentCount(
             @PathVariable Long postId,
             @AuthenticationPrincipal CustomUserDetails user,
             @RequestBody UpdateCountRequestDto requestDto
     ) {
         GroupBuyResponseDto updatedDto = groupBuyService.updateCurrentCount(user.getUser().getEmail(), postId, requestDto);

         return ResponseEntity.ok(updatedDto);
     }

}