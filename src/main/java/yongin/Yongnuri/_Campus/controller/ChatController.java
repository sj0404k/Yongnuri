package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.dto.MypageReq;
import yongin.Yongnuri._Campus.dto.ReportReq;
import yongin.Yongnuri._Campus.dto.chat.*;
import yongin.Yongnuri._Campus.repository.ReportRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.ChatService;
import yongin.Yongnuri._Campus.service.MypageService;
import yongin.Yongnuri._Campus.service.ReportService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final MypageService mypageService;
    private final ReportService reportService;
    private final ReportRepository reportRepository;

    /** 채팅방 목록 조회 */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getChatRooms(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Enum.ChatType type
    ) {
        log.info("get /rooms");
        List<ChatRoomDto> rooms = chatService.getChatRooms(user, type);
        return ResponseEntity.ok(rooms);
    }

    /** 채팅방 세부 조회(입장) — 읽기 전용 */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatEnterRes> getChatRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("roomId") Long roomId
    ) {
        log.info("Get /rooms/{roomId}");
        ChatEnterRes room = chatService.getEnterChatRoom(user, roomId);
        return ResponseEntity.ok(room);
    }

    /** (신규) 읽음/입장 시각 갱신 — 짧은 UPDATE 전용 */
    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markRoomRead(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId
    ) {
        log.info("patch /rooms/{roomId}/read");
        chatService.markRead(user, roomId);
        return ResponseEntity.ok().build();
    }

    /** 채팅방 생성 */
    @PostMapping("/rooms")
    public ResponseEntity<ChatEnterRes> createChatRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ChatRoomReq request
    ) {
        log.info("Post /rooms");
        ChatEnterRes createdRoom = chatService.createChatRoom(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    /** 채팅방 삭제(플래그 관리) */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("roomId") Long roomId
    ) {
        log.info("Delete /rooms/{roomId}");
        chatService.deleteChatRoom(user, roomId);
        return ResponseEntity.noContent().build();
    }

    /** 채팅방 내 중고거래 판매상태 변경 */
    @PatchMapping("/rooms/{roomId}/trade-status")
    public ResponseEntity<Void> updateTradeStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestBody TradeStatusUpdateReq request
    ) {
        log.info("Patch /rooms/{roomId}/trade-status");
        chatService.updateTradeStatus(user, roomId, request.getStatus());
        return ResponseEntity.ok().build();
    }

    /** 메시지 보내기 (웹소켓 라우팅) — ✅ 표준 DTO로 브로드캐스트 */
    @MessageMapping("/rooms/messages")
    public void sendMessage(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestBody ChatMessageRequest message) {
        log.info("Send /rooms/messages");
        ChatMessagesRes res = chatService.saveMessage(user, message);
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), res);
    }

    /** 메시지 보내기 (HTTP) — ✅ 표준 DTO로 반환 */
    @PostMapping("/rooms/messages")
    public ResponseEntity<ChatMessagesRes> sendMessagePost(@AuthenticationPrincipal CustomUserDetails user,
                                                           @RequestBody ChatMessageRequest message) {
        log.info("testPost /rooms/messages");
        ChatMessagesRes res = chatService.saveMessage(user, message);
        return ResponseEntity.ok(res);
    }

    /** ✅ 이미지 메시지 업로드 (multipart/form-data) */
    @PostMapping(
            value = "/rooms/{roomId}/images",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ChatMessagesRes> uploadImageMessage(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @org.springframework.web.bind.annotation.RequestPart("file")
            org.springframework.web.multipart.MultipartFile file
    ) {
        log.info("POST /chat/rooms/{}/images (multipart)", roomId);
        ChatMessagesRes res = chatService.saveImageMessage(user, roomId, file);
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, res);
        return ResponseEntity.ok(res);
    }

    // ===== 신고/차단 =====

    @PostMapping("/rooms/report")
    public ResponseEntity<?> reportChat(@AuthenticationPrincipal CustomUserDetails user,
                                        @RequestBody ReportReq.reportDto reportReq) {
        log.info("post /rooms/report");
        reportReq.setPostType(Enum.ChatType.Chat);
        Long reportedUserId = reportService.reports(user, reportReq);
        if (reportedUserId != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("reportedUserId", reportedUserId);
            response.put("createdAt", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 차단한 유저입니다.");
        }
    }

    @PostMapping("/blocks")
    public ResponseEntity<?> blockChat(@AuthenticationPrincipal CustomUserDetails user,
                                       @RequestBody MypageReq.blocks mypageReq) {
        boolean blocked = mypageService.postBlocks(user.getUser().getEmail(), mypageReq);
        if (blocked) {
            Map<String, Object> response = new HashMap<>();
            response.put("blockedUserId", mypageReq.getBlockedId());
            response.put("blocked", true);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 차단한 유저입니다.");
        }
    }

    @DeleteMapping("/blocks/{blockedUserId}")
    public ResponseEntity<?> unblockChat(@AuthenticationPrincipal CustomUserDetails user,
                                         @PathVariable("blockedUserId") Long blockedUserId) {
        boolean deleted = mypageService.deleteBlocks(user.getUser().getEmail(), blockedUserId);
        if (deleted) {
            return ResponseEntity.ok("차단한 유저 취소 완료");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("차단 내역을 찾을 수 없습니다.");
        }
    }
}
