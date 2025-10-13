package yongin.Yongnuri._Campus.dto.appointment;

import lombok.Getter;

@Getter
public class AppointmentRequestDto {
    private Long postId;
    private Long buyerId;
    private String postType; // "USED_ITEM"
    private Long chatRoomId;
    private String date; // "2025-07-22"
    private String time; // "23:20"
    private String location;
}