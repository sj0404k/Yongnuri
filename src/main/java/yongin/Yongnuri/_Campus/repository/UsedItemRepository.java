package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.UsedItem;

import java.util.List;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long>, JpaSpecificationExecutor<UsedItem> {

    List<UsedItem> findByTitleContainingIgnoreCase(String title);
    List<UsedItem> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<Enum.UsedItemStatus> statuses);

    // (추가) 구매 내역 조회를 위해: 여러 ID와 여러 상태에 해당하는 게시글 목록 조회
    List<UsedItem> findByIdInAndStatusInOrderByCreatedAtDesc(List<Long> ids, List<Enum.UsedItemStatus> statuses);
}
