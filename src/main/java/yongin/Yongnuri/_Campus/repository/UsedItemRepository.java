package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.UsedItem;

import java.time.LocalDateTime;
import java.util.List;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long>, JpaSpecificationExecutor<UsedItem> {

    List<UsedItem> findByTitleContainingIgnoreCase(String title);
    List<UsedItem> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<Enum.UsedItemStatus> statuses);
    List<UsedItem> findByTitleContainingIgnoreCaseAndStatusNot(String title, Enum.UsedItemStatus status);
    List<UsedItem> findByIdInAndStatusInOrderByCreatedAtDesc(List<Long> ids, List<Enum.UsedItemStatus> statuses);
    //자동삭제용
    List<UsedItem> findAllByStatusAndCreatedAtBefore(Enum.UsedItemStatus status, LocalDateTime cutoffDate);
    List<UsedItem> findAllByCreatedAtBefore(LocalDateTime cutoffDate);
}
