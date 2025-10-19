package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.Notification;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.dto.Notificationres;
import yongin.Yongnuri._Campus.repository.NotificationRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository; // 전체 사용자 조회용
    private final FCMService fcmService;
    private final AsyncNotificationService asyncNotificationService;

    public void sendNotification(NotificationRequest request) {
        asyncNotificationService.processNotificationSending(request);
/*
        if (request.isTargetAll()) {
            // 전체 사용자 조회
            request.setTargetUserIds(userRepository.findAllUserIds());
        }
            // 1. 저장할 Notification 객체들을 리스트에 먼저 담습니다.
            List<Notification> notificationsToSave = request.getTargetUserIds().stream()
                    .map(userId -> Notification.builder()
                            .userId(userId)
                            .title(request.getTitle())
                            .message(request.getMessage())
                            .build())
                    .collect(Collectors.toList());
        // 2. 리스트 전체를 한 번에 저장합니다. (DB 호출 1번으로 최적화)
            notificationRepository.saveAll(notificationsToSave);

            // FCM 발송
//            String token = userRepository.findDeviceTokenById(userId);
//            fcmService.sendPush(token, request.getTitle(), request.getMessage());
        */
        }

    @Transactional
    public List<Notificationres> getNotifications(CustomUserDetails user) {
        Long userId = user.getUser().getId();
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        return notifications.stream()
                .map(Notificationres::new)
                .collect(Collectors.toList());
    }
}