package yongin.Yongnuri._Campus.dto.chat;

import lombok.*;
import yongin.Yongnuri._Campus.config.TimeUtils;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomDto {
    private Long id;                    // 방 id
    private Enum.ChatType type;         // 채팅 타입 (중고, 분실, 공동구매 등)
    private String lastMessage;         // 마지막 메시지 내용
    private String updateTime;          // 마지막 메시지 시간(문자열 변환)
    private String toUserNickName;      // 상대방 닉네임

    // ✅ 수정 핵심: 마지막 메시지가 있으면 그 시각으로 updateTime 표시
    public static ChatRoomDto fromEntity(ChatRoom room, User toUser, ChatMessages lastMessage) {
        LocalDateTime timeToShow = (lastMessage != null && lastMessage.getCreatedAt() != null)
                ? lastMessage.getCreatedAt()
                : room.getUpdateTime();

        return ChatRoomDto.builder()
                .id(room.getId())
                .type(room.getType())
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : "")
                .toUserNickName(toUser != null ? toUser.getNickName() : "알 수 없음")
                .updateTime(TimeUtils.toRelativeTime(timeToShow))  // ✅ 핵심
                .build();
    }
}