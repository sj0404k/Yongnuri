package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import yongin.Yongnuri._Campus.domain.UsedItem;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long>, JpaSpecificationExecutor<UsedItem> {
    
}