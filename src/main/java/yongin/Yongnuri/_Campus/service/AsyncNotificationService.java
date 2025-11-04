package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.Notification;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.repository.NotificationRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncNotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FCMService fCMService;

    @Async
    public void processNotificationSending(NotificationRequest request) {
        log.info("비동기 알림 발송 시작. Thread: {}", Thread.currentThread().getName());
        try {
            if (request.isTargetAll()) {
                // 전체 사용자 조회
                request.setTargetUserIds(userRepository.findAllUserIds());
                log.info("보낸 사용자 id : {}",userRepository.findAllUserIds());
            }            // 대상자가 없으면 작업 종료
            if (request.getTargetUserIds() == null || request.getTargetUserIds().isEmpty()){
                log.warn("알림을 보낼 대상자가 없습니다.");
                return;
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
            log.info("{}개의 알림을 DB에 저장했습니다.", notificationsToSave.size());



//            // 4. FCM 푸시 알림을 발송합니다.
//            for (Long userId : request.getTargetUserIds()) {
//                String token = userRepository.findDeviceTokenById(userId);
//                if (token != null && !token.isEmpty()) {
//                    fCMService.sendPush(token, request.getTitle(), request.getMessage());
//                }
//            }




            // FCM  - 혹시 몰라서 남김
//            String token = userRepository.findDeviceTokenById(userId);
//            fcmService.sendPush(token, request.getTitle(), request.getMessage());
        } catch (Exception e) {
            log.error("비동기 알림 발송 중 오류 발생", e);
        }
    }
}