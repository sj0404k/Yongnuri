// repository/UsedItemRepository.java
package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import yongin.Yongnuri._Campus.domain.UsedItem;

public interface UsedItemRepository extends JpaRepository<UsedItem, Long>, JpaSpecificationExecutor<UsedItem> {
    // 기본 CRUD (save, findById, findAll, delete...)는 JpaRepository가 제공
    // 동적 쿼리(findAll(Specification))는 JpaSpecificationExecutor가 제공
}