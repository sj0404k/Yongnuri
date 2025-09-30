package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import yongin.Yongnuri._Campus.domain.UsedItem;

import java.util.List;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long>, JpaSpecificationExecutor<UsedItem> {

    List<UsedItem> findByTitleContainingIgnoreCase(String title);
}