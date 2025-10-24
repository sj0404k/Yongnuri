package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.UsedItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long>, JpaSpecificationExecutor<UsedItem> {

    List<UsedItem> findByTitleContainingIgnoreCase(String title);
    List<UsedItem> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<Enum.UsedItemStatus> statuses);
    List<UsedItem> findByTitleContainingIgnoreCaseAndStatusNot(String title, Enum.UsedItemStatus status);
    List<UsedItem> findByIdInAndStatusInOrderByCreatedAtDesc(List<Long> ids, List<Enum.UsedItemStatus> statuses);
    List<UsedItem> findAllByStatusAndCreatedAtBefore(Enum.UsedItemStatus status, LocalDateTime cutoffDate);
    List<UsedItem> findAllByCreatedAtBefore(LocalDateTime cutoffDate);

    // ✅ 게시글 id → 작성자 userId (연관관계 없이 컬럼 셀렉트)
    @Query("select p.userId from UsedItem p where p.id = :postId")
    Optional<Long> findAuthorIdByPostId(@Param("postId") Long postId);
}
