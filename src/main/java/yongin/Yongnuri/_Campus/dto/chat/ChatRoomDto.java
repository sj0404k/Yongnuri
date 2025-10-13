package yongin.Yongnuri._Campus.dto.chat;

import lombok.*;
import yongin.Yongnuri._Campus.config.TimeUtils;
import yongin.Yongnuri._Campus.domain.ChatRoom;
import yongin.Yongnuri._Campus.domain.Enum;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomDto {
    private Long id;                    //방 id
    private Enum.ChatType type;         //채팅의 타입 ( 전체, 중고, 분실, 공동구매, 관리자)
    private String lastMessage;         //마지막 메시지
    private String updateTime;          //마지막 채팅 보낸 시간
    private Long fromUserId;            //user1
    private Long toUserId;              //user2 아마 게시글 작성자?
    private int count;
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