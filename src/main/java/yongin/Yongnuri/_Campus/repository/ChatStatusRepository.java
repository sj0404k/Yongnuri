package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.ChatStatus;

@Repository
public interface ChatStatusRepository extends JpaRepository<ChatStatus,Long> {
}
