package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import yongin.Yongnuri._Campus.domain.GroupBuy;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long>, JpaSpecificationExecutor<GroupBuy> {
}