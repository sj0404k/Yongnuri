package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.ChatStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatStatusRepository extends JpaRepository<ChatStatus,Long> {
    ChatStatus findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    List<ChatStatus> findByUserIdAndChatStatusTrue(Long userId);

    List<ChatStatus> findByChatRoomId(Long chatRoomId);

    Optional<ChatStatus> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
