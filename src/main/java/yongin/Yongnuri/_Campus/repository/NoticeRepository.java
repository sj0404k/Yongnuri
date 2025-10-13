package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.Notice;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByTitleContainingIgnoreCase(String title);
}
