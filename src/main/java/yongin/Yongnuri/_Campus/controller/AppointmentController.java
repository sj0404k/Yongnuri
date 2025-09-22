package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.appointment.AppointmentRequestDto;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.AppointmentService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/makedeal")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<?> createAppointment(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody AppointmentRequestDto requestDto
    ) {
        Long appointmentId = appointmentService.createAppointment(user.getUser().getEmail(), requestDto);
        return ResponseEntity.ok(Map.of("message", "약속이 생성되었습니다.", "appointmentId", appointmentId));
    }
}