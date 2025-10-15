package yongin.Yongnuri._Campus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.Notification;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notificationres {
    private Long id;
    private String title;
    private String message;
    private Enum.ChatType chatType;
    private Long typeId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public Notificationres(Notification notification) {
    }
    /**
     *     public static enum ChatType {
     *         ALL,        // 전체
     *         USED_ITEM,  // 중고
     *         LOST_ITEM,  // 분실
     *         GROUP_BUY,  // 공동구매
     *         ADMIN,      // 관리자와 채팅
     *         Chat        // 채팅
     *     }
     */
}
