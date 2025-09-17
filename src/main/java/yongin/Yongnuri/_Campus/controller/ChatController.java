package yongin.Yongnuri._Campus.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import yongin.Yongnuri._Campus.domain.ChatMessages;

import yongin.Yongnuri._Campus.dto.ChatMessageRequest;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/message") // 클라이언트가 /pub/chat/message 로 메시지 발행
    public void message(ChatMessageRequest message) {
        // 메시지 처리 로직 (DB 저장, 사용자 알림 등)
        System.out.println("Received message: " + message.getContent() + " from " + message.getSender());

        // 특정 채팅방 구독자에게 메시지 전송
        // 이 예시에서는 topic으로 보냈지만, 1:1 채팅은 /queue/{userId} 형태가 될 수 있습니다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}
