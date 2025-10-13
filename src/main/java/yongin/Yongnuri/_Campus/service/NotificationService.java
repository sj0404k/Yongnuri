package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.Notification;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.repository.NotificationRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository; // 전체 사용자 조회용
    private final FCMService fcmService;

    public void sendNotification(NotificationRequest request) {

        if (request.isTargetAll()) {
            // 전체 사용자 조회
            request.setTargetUserIds(userRepository.findAllUserIds());
        }

        for (Long userId : request.getTargetUserIds()) {
            // DB 저장
            Notification notification = Notification.builder()
                    .userId(userId)
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .build();
            notificationRepository.save(notification);

            // FCM 발송
//            String token = userRepository.findDeviceTokenById(userId);
//            fcmService.sendPush(token, request.getTitle(), request.getMessage());
        }
    }
}