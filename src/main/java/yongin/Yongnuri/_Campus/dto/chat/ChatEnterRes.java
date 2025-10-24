package yongin.Yongnuri._Campus.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yongin.Yongnuri._Campus.domain.*;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.repository.ImageRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatEnterRes {

    private static ImageRepository imageRepository;
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
        private Long opponentId;  // 상대 유저
        private String opponentNickname; //상대유저 채팅방의 제목이 될가능성 높음

        //타입별 추가 필드
        private String title;
        private Enum.LostItemStatus status;        // LOST_ITEM 상태
        private String price;         // USED_ITEM
        private Enum.UsedItemStatus tradeStatus;   // USED_ITEM 거래상태
        private Integer peopleCount;   // GROUP_BUY 인원 표시 (ex. 3/5)
        private String text;
        private String imageUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MessageInfo {
        private Long senderId;
        private String senderNickname;
        private String message;
        private String createdAt;
    }

    public static ChatEnterRes from(ChatRoom room, User opponent, List<ChatMessages> messageList,
                                    Object extraInfo, String thumbnailUrl) {
        RoomInfo.RoomInfoBuilder infoBuilder = RoomInfo.builder()
                .roomId(room.getId())
                .chatType(room.getType())
                .opponentId(opponent.getId())
                .opponentNickname(opponent.getNickName());

        // 🔹 타입별 추가정보 매핑
        if (extraInfo instanceof LostItem lost) {
            infoBuilder.title(lost.getTitle())
                    .status(lost.getStatus());
            infoBuilder.imageUrl(thumbnailUrl);

        } else if (extraInfo instanceof UsedItem used) {
            infoBuilder.title(used.getTitle())
                    .price(String.valueOf(used.getPrice()))
                    .tradeStatus(used.getStatus());
            infoBuilder.imageUrl(thumbnailUrl);

        } else if (extraInfo instanceof GroupBuy group) {
            infoBuilder.title(group.getTitle())
                    .peopleCount(group.getLimit());
            infoBuilder.imageUrl(thumbnailUrl);

        } else if (extraInfo instanceof ChatAdminRes chatAdminRes) {
            infoBuilder.text(chatAdminRes.getText());
        }

        return ChatEnterRes.builder()
                .roomInfo(infoBuilder.build())
                .messages(messageList.stream()
                        .map(m -> MessageInfo.builder()
                                .senderId(m.getSender().getId())
                                .senderNickname(m.getSender().getNickName())
                                .message(m.getMessage())
                                .createdAt(m.getCreatedAt().toString())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

}