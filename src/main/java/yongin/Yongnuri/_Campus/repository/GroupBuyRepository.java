package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import yongin.Yongnuri._Campus.domain.GroupBuy;
import yongin.Yongnuri._Campus.domain.Enum;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long>, JpaSpecificationExecutor<GroupBuy> {
    List<GroupBuy> findByTitleContainingIgnoreCase(String title);
    List<GroupBuy> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<GroupBuy> findByTitleContainingIgnoreCaseAndStatusNot(String title, Enum.GroupBuyStatus status);

    List<GroupBuy> findAllByStatusAndCreatedAtBefore(Enum.GroupBuyStatus status, LocalDateTime cutoffDate);
    List<GroupBuy> findAllByCreatedAtBefore(LocalDateTime cutoffDate);
}
