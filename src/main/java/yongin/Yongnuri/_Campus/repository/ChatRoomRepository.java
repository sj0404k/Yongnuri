package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    List<ChatRoom> findByFromUserIdOrToUserId(Long fromUserId, Long toUserId);
    List<ChatRoom> findByFromUserIdOrToUserIdAndType(Long fromUserId, Long toUserId, ChatRoom.ChatType type);

    Optional<ChatRoom> findByTypeAndTypeIdAndFromUserIdAndToUserId(ChatRoom.ChatType type, Long typeId, Long fromUserId, Long toUserId);
}
