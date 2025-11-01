package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.ChatStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatStatusRepository extends JpaRepository<ChatStatus, Long> {

    ChatStatus findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    List<ChatStatus> findByUserIdAndChatStatusTrue(Long userId);

    List<ChatStatus> findByChatRoomId(Long chatRoomId);

    Optional<ChatStatus> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    /** ✅ lastDate만 단건 업데이트 (조회→save 대신 바로 UPDATE) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ChatStatus cs
           set cs.lastDate = :now
         where cs.chatRoom.id = :roomId
           and cs.user.id = :userId
    """)
    int touchLastDate(@Param("roomId") Long roomId,
                      @Param("userId") Long userId,
                      @Param("now") LocalDateTime now);
}
