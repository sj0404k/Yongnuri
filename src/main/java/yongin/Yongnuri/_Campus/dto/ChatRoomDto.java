package yongin.Yongnuri._Campus.dto;

import lombok.*;
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
    private String type;
    private LocalDateTime updateTime;

    // 엔티티 → DTO 변환
    public static ChatRoomDto fromEntity(ChatRoom room) {
        return ChatRoomDto.builder()
                .id(room.getId())
                .lastMessage(room.getLastMessage())
                .fromUserId(room.getFromUserId())
                .toUserId(room.getToUserId())
                .type(String.valueOf(room.getType()))
                .updateTime(room.getUpdateTime())
                .build();
    }
}