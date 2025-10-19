package yongin.Yongnuri._Campus.dto.chat;

import lombok.Data;
import yongin.Yongnuri._Campus.domain.ChatRoom;
import yongin.Yongnuri._Campus.domain.Enum;

@Data
public class TradeStatusUpdateReq {
    private Enum.UsedItemStatus status;
}
