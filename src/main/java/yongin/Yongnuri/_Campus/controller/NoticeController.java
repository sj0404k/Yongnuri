package yongin.Yongnuri._Campus.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yongin.Yongnuri._Campus.domain.AllNotice;
import yongin.Yongnuri._Campus.domain.Notice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.dto.notice.AllNoticeDto;
import yongin.Yongnuri._Campus.dto.notice.NoticeCreateRequestDto;
import yongin.Yongnuri._Campus.dto.notice.NoticeUpdateRequestDto;
import yongin.Yongnuri._Campus.dto.notice.NoticeResponseDto;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.NoticeService;
import yongin.Yongnuri._Campus.service.NotificationService;

import java.util.Collection;
import java.util.List;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NoticeResponseDto>> getNotices(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<NoticeResponseDto> items = noticeService.getNotices(user.getUser().getEmail());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<NoticeResponseDto> getNoticeDetail(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        NoticeResponseDto item = noticeService.getNoticeDetail(user.getUser().getEmail(), postId);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/allnotice")
    public ResponseEntity<?> createAllNotice( 
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AllNoticeDto requestDto){

        noticeService.allCreateNotice(user.getUser().getEmail() ,requestDto);

        return ResponseEntity.ok("All공지사항 작성 완료");
    }
    @GetMapping("/allnoticedetail")
    public AllNotice getAllNoticesDetail(@AuthenticationPrincipal CustomUserDetails user, Long postId){
        return noticeService.getAllNoticeDetail(postId);

    }
    @GetMapping("/allnotice")
    public List<AllNotice> getAllNotices(@AuthenticationPrincipal CustomUserDetails user, Long postId){
        return noticeService.getAllNotice();

    }
    //공지홍보 게시글 작성

    @PostMapping
    public ResponseEntity<?> createNotice(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody NoticeCreateRequestDto requestDto) {
        Long noticeId = noticeService.createNotice(requestDto, user.getUser().getEmail());
        // 2. NotificationRequest 생성
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("새 공지사항: " + requestDto.getTitle());
        notificationRequest.setMessage(requestDto.getContent());
        notificationRequest.setTargetAll(true); // 전체 사용자에게 알림 전송용 플래그

        // 3. NotificationService 호출
        notificationService.sendNotification(notificationRequest);
        return ResponseEntity.ok(Map.of("message", "공지사항 작성 성공", "noticeId", noticeId));
    }

    @PatchMapping("/{noticeId}")
    public ResponseEntity<?> updateNotice(
            @PathVariable("noticeId") Long noticeId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody NoticeUpdateRequestDto requestDto) {
        noticeService.updateNotice(noticeId, requestDto, user.getUser().getEmail());
        return ResponseEntity.ok("공지사항이 수정되었습니다.");
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable("noticeId") Long noticeId,
            @AuthenticationPrincipal CustomUserDetails user) {
        noticeService.deleteNotice(noticeId, user.getUser().getEmail());
        return ResponseEntity.ok("공지사항이 삭제되었습니다.");
    }
}