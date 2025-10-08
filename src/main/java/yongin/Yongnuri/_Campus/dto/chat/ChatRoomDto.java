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
    private Long id;                    //방 id
    private String lastMessage;         //마지막 메시지
    private Long fromUserId;            //user1
    private Long toUserId;              //user2
    private ChatRoom.ChatType type;     //채팅의 타입
    private String updateTime;          //마지막 채팅 보낸 시간

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