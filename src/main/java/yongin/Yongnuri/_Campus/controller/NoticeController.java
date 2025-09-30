package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/board/notices")
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
}