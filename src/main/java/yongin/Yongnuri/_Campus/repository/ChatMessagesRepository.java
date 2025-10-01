package yongin.Yongnuri._Campus.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.ChatMessages;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
//    Optional<Object> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    Slice<ChatMessages> findByChatRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    List<ChatMessages> findByChatRoomId(Long chatRoomId);
}
