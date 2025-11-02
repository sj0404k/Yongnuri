package yongin.Yongnuri._Campus.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.ChatMessages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
//    Optional<Object> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    Slice<ChatMessages> findByChatRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    List<ChatMessages> findByChatRoomId(Long chatRoomId);

    Optional<ChatMessages> findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    List<ChatMessages> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    @Query("""
        SELECT m FROM ChatMessages m
        WHERE m.chatRoom.id = :chatRoomId
          AND (m.createdAt > :deletedAt OR :deletedAt IS NULL)
        ORDER BY m.createdAt ASC
    """)
    List<ChatMessages> findMessagesAfterDeletedAt(@Param("chatRoomId") Long chatRoomId, @Param("deletedAt") LocalDateTime deletedAt);
    // [추가] 여러 채팅방의 마지막 메시지를 한 번의 쿼리로 가져오기 (N+1 최적화)
    @Query(value = "SELECT m.* FROM ( " +
            "    SELECT *, ROW_NUMBER() OVER (PARTITION BY chat_room_id ORDER BY created_at DESC) as rn " +
            "    FROM chat_messages WHERE chat_room_id IN :roomIds " +
            ") m WHERE m.rn = 1", nativeQuery = true)
    List<ChatMessages> findLastMessagesByRoomIds(@Param("roomIds") List<Long> roomIds);
    @Query("SELECT m.message FROM ChatMessages m WHERE m.chatType = 'img'")
    List<String> findAllImageUrls();
}
