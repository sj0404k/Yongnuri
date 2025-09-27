package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yongin.Yongnuri._Campus.domain.Notice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.notice.NoticeResponseDto;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.NoticeService;
import java.util.List;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<List<NoticeResponseDto>> getNotices(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<NoticeResponseDto> items = noticeService.getNotices(user.getUser().getEmail());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<NoticeResponseDto> getNoticeDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        NoticeResponseDto item = noticeService.getNoticeDetail(user.getUser().getEmail(), postId);
        return ResponseEntity.ok(item);
    }
    @PostMapping
    public Notice createNotice(@RequestBody Map<String, Object> payload) {
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        boolean isImages = payload.get("isImages") != null && (Boolean) payload.get("isImages");
        String link = (String) payload.get("link");
        String startDate = (String) payload.get("startDate");
        String endDate = (String) payload.get("endDate");

        return noticeService.saveNotice(userId, title, content, isImages, link, startDate, endDate);
    }
}