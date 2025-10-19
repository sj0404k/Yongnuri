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
    private Long id;                    //방 id
    private Enum.ChatType type;         //채팅의 타입 ( 전체, 중고, 분실, 공동구매, 관리자)
    private String lastMessage;         //마지막 메시지
    private String updateTime;          //마지막 채팅 보낸 시간
    private String toUserNickName;              //user2 아마 게시글 작성자? 상대방

    // 엔티티 → DTO 변환
    public static ChatRoomDto fromEntity(ChatRoom room, User toUser, ChatMessages chatMessages) {
        return ChatRoomDto.builder()
                .id(room.getId())
                .lastMessage(chatMessages != null
                        ? chatMessages.getMessage()
                        : "" )
                .toUserNickName(toUser.getNickName())
                .type(room.getType())
                .updateTime(TimeUtils.toRelativeTime(room.getUpdateTime()))

                .build();
    }
}