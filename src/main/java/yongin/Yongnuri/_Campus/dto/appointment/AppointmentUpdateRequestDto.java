package yongin.Yongnuri._Campus.dto.appointment;

import lombok.Getter;

@Getter
public class AppointmentUpdateRequestDto {
    private String date;
    private String time;
    private String location;
    private String status; // "SCHEDULED", "COMPLETED", "CANCELED"
}