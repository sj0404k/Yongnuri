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
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // ✅ 분실물(또는 다른 타입) 게시글 ID 기준으로 방 목록 조회 (웹소켓 브로드캐스트용)
    List<ChatRoom> findByTypeAndTypeId(Enum.ChatType type, Long typeId);

    // ✅ 동시성 높을 때(예: 상태 일괄 갱신) 사용 가능한 비관적 락 + participants 즉시 로딩
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN FETCH cr.participants p WHERE cr.type = :type AND cr.typeId = :typeId")
    List<ChatRoom> findByTypeAndTypeIdWithParticipantsAndLock(
            @Param("type") Enum.ChatType type,
            @Param("typeId") Long typeId
    );

    // ✅ 목록 조회 시 N+1 방지: participants, user까지 즉시 로딩
    @Query("SELECT DISTINCT cr FROM ChatRoom cr JOIN FETCH cr.participants p JOIN FETCH p.user WHERE cr.id IN :roomIds")
    List<ChatRoom> findByIdInWithParticipants(@Param("roomIds") List<Long> roomIds);

    // ✅ 타입 필터 포함 버전
    @Query("SELECT DISTINCT cr FROM ChatRoom cr JOIN FETCH cr.participants p JOIN FETCH p.user WHERE cr.id IN :roomIds AND cr.type = :type")
    List<ChatRoom> findByIdInAndTypeWithParticipants(@Param("roomIds") List<Long> roomIds, @Param("type") Enum.ChatType type);

    // 일반 헬퍼들
    List<ChatRoom> findByIdIn(List<Long> activeRoomIds);
    List<ChatRoom> findByIdInAndType(List<Long> activeRoomIds, Enum.ChatType chatType);

    // 유저가 속한 타입별 방 찾기
    @Query("SELECT r FROM ChatRoom r JOIN r.participants p WHERE r.type = :type AND p.user.id = :userId")
    Optional<ChatRoom> findByTypeAndParticipantsUserId(@Param("type") Enum.ChatType type, @Param("userId") Long userId);

    @Query("SELECT r FROM ChatRoom r JOIN FETCH r.participants WHERE r.id IN :ids AND r.type <> :excludedType")
    List<ChatRoom> findByIdInAndTypeNotWithParticipants(@Param("ids") List<Long> ids,
                                                        @Param("excludedType") Enum.ChatType excludedType);

}
