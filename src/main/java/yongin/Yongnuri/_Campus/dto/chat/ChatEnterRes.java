package yongin.Yongnuri._Campus.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;

import java.util.Collections; // ✅ [추가]
import java.util.List;
import java.util.Map; // ✅ [추가]
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
        private Long chatTypeId;
        private Long opponentId;
        private String opponentNickname;
        private String title;
        private Enum.LostItemStatus status;
        private String price;
        private Enum.UsedItemStatus tradeStatus;
        private Integer peopleCount;
        private String text;
        private String imageUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MessageInfo {
        // ... (내용 동일, imageUrls 필드 확인)
        private Long senderId;
        private String senderEmail;
        private String senderNickname;
        private String message;
        private String createdAt;
        private ChatMessages.messageType chatType;
        private List<String> imageUrls; //  메시지 이미지 URL
    }

    private static String lower(String s) {
        return s == null ? null : s.toLowerCase();
    }

    // ✅ [수정] from 메서드 시그니처에 'imagesByMessageId' 파라미터 추가
    public static ChatEnterRes from(ChatRoom room,
                                    User opponent,
                                    List<ChatMessages> messageList,
                                    Object extraInfo,
                                    String thumbnailUrl,
                                    Map<Long, List<String>> imagesByMessageId) { // <-- ✅ [추가]

        RoomInfo.RoomInfoBuilder infoBuilder = RoomInfo.builder()
                .roomId(room.getId())
                .chatType(room.getType())
                .chatTypeId(room.getTypeId())
                .opponentId(opponent != null ? opponent.getId() : null)
                .opponentNickname(opponent != null ? opponent.getNickName() : "상대방")
                .imageUrl(thumbnailUrl);

        // 🔹 타입별 추가정보 매핑 (내용 동일)
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

        // ✅ [수정] MessageInfo 생성 시 'imageUrls' 필드 채우기
        List<MessageInfo> msgs = messageList.stream()
                .map(m -> MessageInfo.builder()
                        .senderId(m.getSender() != null ? m.getSender().getId() : null)
                        .senderEmail(m.getSender() != null ? lower(m.getSender().getEmail()) : null) // ✅ 추가
                        .senderNickname(m.getSender() != null ? m.getSender().getNickName() : null)
                        .message(m.getMessage())
                        .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null)
                        .chatType(m.getChatType())
                        // ✅ [추가] 해당 메시지 ID의 이미지 리스트를 맵에서 찾아 설정 (없으면 빈 리스트)
                        .imageUrls(imagesByMessageId.getOrDefault(m.getId(), Collections.emptyList()))
                        .build())
                .collect(Collectors.toList());

        return ChatEnterRes.builder()
                .roomInfo(infoBuilder.build())
                .messages(msgs)
                .build();
    }
}