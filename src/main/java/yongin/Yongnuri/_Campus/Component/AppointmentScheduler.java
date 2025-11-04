package yongin.Yongnuri._Campus.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yongin.Yongnuri._Campus.domain.Appointment;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.repository.AppointmentRepository;
import yongin.Yongnuri._Campus.service.NotificationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    /**
     * 매시 0분 0초 (정각)에 실행됩니다.
     * (예: 13:00, 14:00, 15:00...)
     * 3시간 뒤 (예: 10시 정각 실행 -> 13:00:00 ~ 13:59:59) 사이에 잡힌 약속을 찾습니다.
     */
    @Scheduled(cron = "0 0 * * * ?") //  매시 0분(정각)에 실행
    @Transactional
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();

        //  3시간 뒤 정각 ~ 4시간 뒤 정각 사이의 약속을 찾습니다.
        LocalDateTime startTime = now.plusHours(3).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(1);

        // 1. DB에서 3시간 뒤에 예정된(SCHEDULED) 약속 목록을 조회합니다.
        List<Appointment> upcomingAppointments = appointmentRepository.findAllByStatusAndAppointmentAtBetween(
                Enum.AppointmentStatus.SCHEDULED,
                startTime,
                endTime
        );

        if (upcomingAppointments.isEmpty()) {
            return; // 보낼 알림이 없으면 종료
        }

        System.out.println(upcomingAppointments.size() + "건의 약속 알림을 발송합니다.");

        // 2. 각 약속의 판매자와 구매자에게 알림을 보냅니다.
        for (Appointment appt : upcomingAppointments) {
            try {
                String title = "[게시글] 거래자와 약속 3시간 전이에요!";
                String message = "물품을 챙겨 늦지 않게 약속 장소에 도착하세요!";

                // 판매자에게 알림 발송
                NotificationRequest requestToSeller = new NotificationRequest();
                requestToSeller.setTitle(title);
                requestToSeller.setMessage(message);
                requestToSeller.setUserId(appt.getSellerId());
                notificationService.sendNotification(requestToSeller);

                // 구매자에게 알림 발송
                NotificationRequest requestToBuyer = new NotificationRequest();
                requestToBuyer.setTitle(title);
                requestToBuyer.setMessage(message);
                requestToBuyer.setUserId(appt.getBuyerId());
                notificationService.sendNotification(requestToBuyer);

            } catch (Exception e) {
                System.err.println("약속 ID " + appt.getId() + " 알림 전송 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
