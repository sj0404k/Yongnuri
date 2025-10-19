package yongin.Yongnuri._Campus.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ChatNoticeDto {
    private String notice;
    private List<Message> messages; // 채팅 메시지 목록

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Message {
        private Long userId;
        private String nickname;
        private boolean isMine; // 내가 보낸 메시지인지 여부
        private String message;
        private LocalDateTime time;
    }
}
