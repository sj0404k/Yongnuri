package yongin.Yongnuri._Campus.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatEnterRes {

    private RoomInfo roomInfo;
    private List<MessageInfo> messages;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RoomInfo {
        private Long roomId;
        private Enum.ChatType chatType;
        private Long chatTypeId;              // ✅ 게시글/연결 ID
        private Long opponentId;              // 상대 유저 ID (null-safe)
        private String opponentNickname;      // 상대 닉네임 (null-safe)

        // 타입별 추가 필드
        private String title;
        private Enum.LostItemStatus status;   // LOST_ITEM 상태
        private String price;                 // USED_ITEM
        private Enum.UsedItemStatus tradeStatus; // USED_ITEM 거래상태
        private Integer peopleCount;          // GROUP_BUY 인원 표시 (ex. 제한)
        private String text;                  // ADMIN 텍스트 등
        private String imageUrl;              // 썸네일
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MessageInfo {
        private Long senderId;          // ✅ 필수
        private String senderEmail;     // ✅ 필수(소문자)
        private String senderNickname;  // 표시용
        private String message;
        private String createdAt;       // ISO 문자열 (LocalDateTime#toString)
        private ChatMessages.messageType chatType; //메시지 타입
    }

    private static String lower(String s) {
        return s == null ? null : s.toLowerCase();
    }

    public static ChatEnterRes from(ChatRoom room,
                                    User opponent,
                                    List<ChatMessages> messageList,
                                    Object extraInfo,
                                    String thumbnailUrl) {

        RoomInfo.RoomInfoBuilder infoBuilder = RoomInfo.builder()
                .roomId(room.getId())
                .chatType(room.getType())
                .chatTypeId(room.getTypeId()) // ✅ 누락 보완
                .opponentId(opponent != null ? opponent.getId() : null)
                .opponentNickname(opponent != null ? opponent.getNickName() : "상대방")
                .imageUrl(thumbnailUrl);

        // 🔹 타입별 추가정보 매핑
        if (extraInfo instanceof LostItem lost) {
            infoBuilder.title(lost.getTitle())
                    .status(lost.getStatus());

        } else if (extraInfo instanceof UsedItem used) {
            infoBuilder.title(used.getTitle())
                    .price(String.valueOf(used.getPrice()))
                    .tradeStatus(used.getStatus());

        } else if (extraInfo instanceof GroupBuy group) {
            infoBuilder.title(group.getTitle())
                    .peopleCount(group.getLimit());

        } else if (extraInfo instanceof ChatAdminRes chatAdminRes) {
            infoBuilder.text(chatAdminRes.getText());
        }

        List<MessageInfo> msgs = messageList.stream()
                .map(m -> MessageInfo.builder()
                        .senderId(m.getSender() != null ? m.getSender().getId() : null)
                        .senderEmail(m.getSender() != null ? lower(m.getSender().getEmail()) : null) // ✅ 추가
                        .senderNickname(m.getSender() != null ? m.getSender().getNickName() : null)
                        .message(m.getMessage())
                        .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null)
                        .chatType(m.getChatType())
                        .build())
                .collect(Collectors.toList());

        return ChatEnterRes.builder()
                .roomInfo(infoBuilder.build())
                .messages(msgs)
                .build();
    }
}
