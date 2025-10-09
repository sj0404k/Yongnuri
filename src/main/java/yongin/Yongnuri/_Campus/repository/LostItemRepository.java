package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.domain.Enum;
import java.util.List;

public interface LostItemRepository extends JpaRepository<LostItem, Long>, JpaSpecificationExecutor<LostItem> {
   List<LostItem> findByTitleContainingIgnoreCase(String title);

    List<LostItem> findByUserAndPurposeOrderByCreatedAtDesc(User user, Enum.LostItemPurpose purpose);


    List<LostItem> findByUserAndStatusOrderByCreatedAtDesc(User user, Enum.LostItemStatus status);

}