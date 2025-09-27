package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.Notice;
import yongin.Yongnuri._Campus.repository.NoticeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Notice saveNotice(Long userId, String title, String content, boolean isImages, String link,
                             String startDateStr, String endDateStr) {
        // startDateStr, endDateStr: "2025-09-05" 형식
        LocalDateTime startDate = (startDateStr != null) ? LocalDateTime.parse(startDateStr) : null;
        LocalDateTime endDate = (endDateStr != null) ? LocalDateTime.parse(endDateStr) : null;

        Notice notice = Notice.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .isImges(isImages)
                .link(link)
                .startDate(startDate)
                .endDate(endDate)
                .createdAt(LocalDateTime.now())
                .build();

        return noticeRepository.save(notice);
    }
}
