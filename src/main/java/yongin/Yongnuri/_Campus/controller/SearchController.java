package yongin.Yongnuri._Campus.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.BlocksRes;
import yongin.Yongnuri._Campus.dto.SearchReq;
import yongin.Yongnuri._Campus.dto.SearchRes;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.SearchService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@AllArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchRes>> search(@AuthenticationPrincipal CustomUserDetails user) {
        List<SearchRes> searchDto = searchService.getHistory(user.getUser().getEmail());
        return ResponseEntity.ok(searchDto);
    }
    // 검색 기록 추가
    @PostMapping("/history")
    public ResponseEntity<?> addSearchHistory(@AuthenticationPrincipal CustomUserDetails user, @RequestBody SearchReq searchReq) {
        searchService.addHistory(user.getUser().getEmail(), searchReq);
        return ResponseEntity.ok("전체 검색 기록 삭제 완료");
    }
    // 검색 히스토리 조회
    @GetMapping("/history")
    public ResponseEntity<List<SearchRes>> getSearchHistory(@AuthenticationPrincipal CustomUserDetails user) {
        List<SearchRes> searchDto = searchService.getHistory(user.getUser().getEmail());
        return ResponseEntity.ok(searchDto);
    }

    // 특정 검색 기록 삭제
    @DeleteMapping("/history/{searchId}")
    public ResponseEntity<?> deleteSearchHistory(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("searchId") Long searchId) {
       boolean deleted = searchService.deleteHistoryById(user.getUser().getEmail(), searchId);

        if (deleted) {
            return ResponseEntity.ok("검색 기록 완료");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("검색 기록을 찾을 수 없습니다.");
        }

    }

    // 전체 검색 기록 삭제
    @DeleteMapping("/history")
    public ResponseEntity<?>  deleteAllSearchHistory(@AuthenticationPrincipal CustomUserDetails user) {
        searchService.deleteAllHistory(user.getUser().getEmail());
        return ResponseEntity.ok("전체 검색 기록 삭제 완료");
    }


}
