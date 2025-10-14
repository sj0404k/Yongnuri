package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.domain.ChatRoom;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.dto.MypageReq;
import yongin.Yongnuri._Campus.dto.ReportReq;
import yongin.Yongnuri._Campus.dto.chat.ChatMessageRequest;
import yongin.Yongnuri._Campus.dto.chat.ChatMessagesRes;
import yongin.Yongnuri._Campus.dto.chat.ChatRoomDto;
import yongin.Yongnuri._Campus.dto.chat.ChatRoomReq;
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
public class ChatController {
    /*
        우선 기능별 컨드롤만 작성
     */
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final MypageService mypageService;
    private final ReportService reportService;
    private final ReportRepository reportRepository;
/**
 * 필요 기능들
 * 채팅방 목록 조회 (닉네임 시간 마지막 내용) ----  완료
 * 채팅방 세부 조회
 * 메시지 조회 -- 세부조화랑 갇을듯?
 * 읽음처리- 이건 세부 조회할때 처리하면 될듯
 * 채팅방 내 중고거래 판매상태 변경(판매자만 가능하게)
 * 메시지 보내기
 * 메시지 보내기 (택스트, 이미지)
 *
 * 채칭방 신고
 * 사용자 차단
 * 사용자 차단해제
 */

    /**
     * 채팅방 목록 조회
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getChatRooms(@AuthenticationPrincipal CustomUserDetails user, @RequestBody Enum.ChatType type) {
        List<ChatRoomDto> rooms = chatService.getChatRooms(user, type);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 채팅방 세부 조회 (채팅방 입장하기)
     */
    @GetMapping("/rooms/{RoomId}")
    public ResponseEntity<List<ChatMessagesRes>> getChatRoom(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("RoomId") Long RoomId) {
        List<ChatMessagesRes> room = chatService.getEnterChatRoom(user, RoomId);
        return ResponseEntity.ok(room);
    }

    /**
     * 채팅방 생성
     */
    @PostMapping
    public ResponseEntity<?> createChatRoom(@AuthenticationPrincipal CustomUserDetails user, @RequestBody ChatRoomReq request) {
        chatService.createChatRoom(user, request);
        return ResponseEntity.ok("채팅방 활성화");
    }

    /**
     * 채팅방 삭제 방법 완전삭제가 아닌 플래그로 관리
     * 채팅방을 가지고 있는 사람들이 모두 해당 방을 지워야지 방삭제가 됨
     * 초기 방생성시 chatStatus true 필요  -- 만들러감
     */
    @DeleteMapping
    public ResponseEntity<?> deleteRoom(@AuthenticationPrincipal CustomUserDetails user, @RequestBody Long chatRoomId) {
        chatService.deleteChatRoom(user, chatRoomId);
        return ResponseEntity.ok("방 삭제 완료");
    }


//    /** 메시지 보내기 */
//    @PostMapping("/{chatRoomId}/messages")
//    public ResponseEntity<?> sendMessage(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long chatRoomId, @RequestBody ChatMessageRequest message) {
//
//        ChatMessages saved = chatService.saveMessage(user, chatRoomId, message);
//
//        // 웹소켓 구독자에게도 메시지 발행
//        messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoomId, message);
//
//        return ResponseEntity.ok(saved);
//    }

    // 테스트용 api
    @MessageMapping("/chat/message") // 클라이언트가 /pub/chat/message 로 메시지 발행
    public void message(ChatMessageRequest message) {
        // 메시지 처리 로직 (DB 저장, 사용자 알림 등)
        System.out.println("message: " + message.getContent() + " from " + message.getSender());

        // 특정 채팅방 구독자에게 메시지 전송
        // 이 예시에서는 topic으로 보냈지만, 1:1 채팅은 /queue/{userId} 형태가 될 수 있습니다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }

    @GetMapping("/notice")
    public ResponseEntity<?> getNoticeChat(@AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok("공지채팅 가져옴");
    }
//    //문의하기
//    @MessageMapping("/notice")
//    public void notice(ChatMessageRequest message) {
//
//    }


    // 채팅 리폿 관련 부분
    @PostMapping("/rooms/report")
    public ResponseEntity<?> reportChat(@AuthenticationPrincipal CustomUserDetails user, @RequestBody ReportReq.reportDto reportReq) {
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
    public ResponseEntity<?> blockChat(@AuthenticationPrincipal CustomUserDetails user, @RequestBody MypageReq.blocks mypageReq) {
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
    public ResponseEntity<?> unblockChat(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("blockedUserId") Long blockedUserId) {
        boolean deleted = mypageService.deleteBlocks(user.getUser().getEmail(), blockedUserId);

        if (deleted) {
            return ResponseEntity.ok("차단한 유저 취소 완료");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("차단 내역을 찾을 수 없습니다.");
        }
    }
}
