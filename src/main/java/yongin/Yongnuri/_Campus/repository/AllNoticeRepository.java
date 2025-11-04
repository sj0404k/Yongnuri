package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.AllNotice;

import java.util.Optional;

@Repository
public interface AllNoticeRepository extends JpaRepository<AllNotice, Integer> {
    Optional<AllNotice> findById(Long postId);
}
