package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

    List<ChatRoom> findByIdIn(List<Long> activeRoomIds);

    List<ChatRoom> findByIdInAndType(List<Long> activeRoomIds, Enum.ChatType chatType);

    List<ChatRoom> findByTypeAndTypeId(Enum.ChatType type, Long typeId);
}
