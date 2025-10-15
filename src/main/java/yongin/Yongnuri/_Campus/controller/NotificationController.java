package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yongin.Yongnuri._Campus.dto.NotificationRequest;
import yongin.Yongnuri._Campus.dto.Notificationres;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     *
     * @param request userId, targetUserIds, targetAll, title ,message
     * @return 나중에 점함.
     */
    @PostMapping("/send")
    public String sendNotification(@AuthenticationPrincipal CustomUserDetails user , @RequestBody NotificationRequest  request) {

        notificationService.sendNotification(request);
        return "공지 알람 저장 및 보냄";
    }

    /**
     * isRead 값 변경넣어야됨
     * @return id, title, message, chatType, typeId, isRead createdAt;
     */
    @GetMapping
    public List<Notificationres> getNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        return notificationService.getNotifications(user);

    }

}