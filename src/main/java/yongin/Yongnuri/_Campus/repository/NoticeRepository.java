package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.Notice;
import yongin.Yongnuri._Campus.domain.Enum;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByTitleContainingIgnoreCase(String title);
    List<Notice> findByTitleContainingIgnoreCaseAndStatusNot(String title, Enum.NoticeStatus status);

    List<Notice> findAllByStatusAndCreatedAtBefore(Enum.NoticeStatus status, LocalDateTime cutoffDate);
    List<Notice> findAllByCreatedAtBefore(LocalDateTime cutoffDate);
}
