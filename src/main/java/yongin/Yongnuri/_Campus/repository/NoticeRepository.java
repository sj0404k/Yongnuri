package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yongin.Yongnuri._Campus.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}