package yongin.Yongnuri._Campus.dto.chat;

import lombok.*;
import yongin.Yongnuri._Campus.config.TimeUtils;
import yongin.Yongnuri._Campus.domain.ChatRoom;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomDto {
    private Long id;
    private String lastMessage;
    private Long fromUserId;
    private Long toUserId;
    private ChatRoom.ChatType type;
    private String updateTime;

    // 엔티티 → DTO 변환
    public static ChatRoomDto fromEntity(ChatRoom room) {
        return ChatRoomDto.builder()
                .id(room.getId())
                .lastMessage(room.getLastMessage())
                .fromUserId(room.getFromUserId())
                .toUserId(room.getToUserId())
                .type(room.getType())
                .updateTime(TimeUtils.toRelativeTime(room.getUpdateTime()))
                .build();
    }
}