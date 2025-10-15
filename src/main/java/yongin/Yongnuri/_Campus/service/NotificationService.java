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

    @Transactional
    public List<Notificationres> getNotifications(CustomUserDetails user) {
        Long userId = user.getUser().getId();
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        return notifications.stream()
                .map(Notificationres::new)
                .collect(Collectors.toList());
    }
}