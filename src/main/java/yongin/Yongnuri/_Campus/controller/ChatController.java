package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.domain.ChatMessages;
import yongin.Yongnuri._Campus.dto.ChatMessageRequest;
import yongin.Yongnuri._Campus.dto.ChatRoomDto;
import yongin.Yongnuri._Campus.dto.ChatRoomReq;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.servise.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;


    /** 채팅방 목록 조회 */
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> getChatRooms(@AuthenticationPrincipal CustomUserDetails user, @RequestBody String type) {
        List<ChatRoomDto> rooms = chatService.getChatRooms(user, type);
        return ResponseEntity.ok(rooms);
    }

    /** 채팅방 세부 조회 */
//    @GetMapping("/{chatRoomId}")
//    public ResponseEntity<ChatRoomDto> getChatRoom(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long chatRoomId) {
//        ChatRoomDto room = chatService.getChatRoom(user, chatRoomId);
//        return ResponseEntity.ok(room);
//    }

    /** 채팅방 생성 */
    @PostMapping
    public ResponseEntity<?> createChatRoom(@AuthenticationPrincipal CustomUserDetails user, @RequestBody ChatRoomReq request) {
         chatService.createChatRoom(user, request);
        return ResponseEntity.ok("채팅방 활성화");
    }
//
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

}
