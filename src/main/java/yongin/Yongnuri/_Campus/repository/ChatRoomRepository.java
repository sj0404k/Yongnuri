package yongin.Yongnuri._Campus.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.ChatRoom;
import yongin.Yongnuri._Campus.domain.Enum;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

//    List<ChatRoom> findByFromUserIdOrToUserId(Long fromUserId, Long toUserId);
//    List<ChatRoom> findByFromUserIdOrToUserIdAndType(Long fromUserId, Long toUserId, Enum.ChatType type);
//    Optional<ChatRoom> findById(Long Id);
//    Optional<ChatRoom> findByTypeAndTypeIdAndFromUserIdAndToUserId(Enum.ChatType type, Long typeId, Long fromUserId, Long toUserId);
// [수정] 비관적 락을 적용하여 동시성 문제를 해결하고, JOIN FETCH로 N+1 방지
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT cr FROM ChatRoom cr LEFT JOIN FETCH cr.participants p WHERE cr.type = :type AND cr.typeId = :typeId")
List<ChatRoom> findByTypeAndTypeIdWithParticipantsAndLock(
        @Param("type") Enum.ChatType type,
        @Param("typeId") Long typeId
);

    // [수정] 채팅방 목록 조회 시 N+1 문제 해결을 위해 JOIN FETCH 사용
    @Query("SELECT DISTINCT cr FROM ChatRoom cr JOIN FETCH cr.participants p JOIN FETCH p.user WHERE cr.id IN :roomIds")
    List<ChatRoom> findByIdInWithParticipants(@Param("roomIds") List<Long> roomIds);

    // [수정] 타입 필터링이 추가된 버전
    @Query("SELECT DISTINCT cr FROM ChatRoom cr JOIN FETCH cr.participants p JOIN FETCH p.user WHERE cr.id IN :roomIds AND cr.type = :type")
    List<ChatRoom> findByIdInAndTypeWithParticipants(@Param("roomIds") List<Long> roomIds, @Param("type") Enum.ChatType type);

    List<ChatRoom> findByIdIn(List<Long> activeRoomIds);

    List<ChatRoom> findByIdInAndType(List<Long> activeRoomIds, Enum.ChatType chatType);

    List<ChatRoom> findByTypeAndTypeId(Enum.ChatType type, Long typeId);
}
