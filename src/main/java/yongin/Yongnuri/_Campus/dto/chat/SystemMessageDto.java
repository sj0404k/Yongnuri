package yongin.Yongnuri._Campus.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅방 내 시스템 메시지 (예: 분실물 회수 완료 등)
 * - 프론트에서 type === "system" 으로 구분해 출력
 * - FCM(Notificationres)과는 별도
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessageDto {
    private String type = "system"; // 항상 system
    private String text;            // 메시지 본문
    private long timestamp;         // UNIX time(ms)

    public static SystemMessageDto of(String text) {
        SystemMessageDto dto = new SystemMessageDto();
        dto.setText(text);
        dto.setTimestamp(System.currentTimeMillis());
        return dto;
    }
}
