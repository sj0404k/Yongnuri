package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.Notification;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;

    public void sendNotification(NotificationRequest request) {

        // DB에 저장
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .build();
        notificationRepository.save(notification);

        // FCM 푸시 발송
//        fcmService.sendPush(request.getToken(), request.getTitle(), request.getMessage());
    }
}