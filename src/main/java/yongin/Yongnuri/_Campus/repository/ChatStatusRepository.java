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

    List<ChatStatus> findByChatRoomId(Long chatRoomId);

    Optional<ChatStatus> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    List<ChatStatus> findByUserIdAndChatStatusTrue(Long userId);

    // leftAt IS NULL (나가지 않은) 방을 찾음
    List<ChatStatus> findByUserIdAndLeftAtIsNull(Long userId);
    // 방 ID로 모든 참여자 상태를 가져올 때 User 정보도 함께 가져옴
    @Query("SELECT cs FROM ChatStatus cs JOIN FETCH cs.user WHERE cs.chatRoom.id = :roomId")
    List<ChatStatus> findByChatRoomIdWithUser(@Param("roomId") Long roomId);
    // 방 ID 목록으로 모든 참여자 상태를 가져올 때 User 정보도 함께 가져옴
    @Query("SELECT cs FROM ChatStatus cs JOIN FETCH cs.user WHERE cs.chatRoom.id IN :roomIds")
    List<ChatStatus> findByChatRoomIdInWithUser(@Param("roomIds") List<Long> roomIds);
    //나가지 않은 방 또는 나갔지만 새 메시지가 온 방을 모두 조회
    @Query("SELECT cs FROM ChatStatus cs " +
            "JOIN FETCH cs.chatRoom cr " +
            "WHERE cs.user.id = :userId " +
            "AND (cs.leftAt IS NULL OR cr.updateTime > cs.leftAt)")
    List<ChatStatus> findActiveChatRoomsForUser(@Param("userId") Long userId);

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
