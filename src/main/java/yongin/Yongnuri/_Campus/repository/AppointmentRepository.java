package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yongin.Yongnuri._Campus.domain.Appointment;
import java.util.List;
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    void deleteAllByPostTypeAndPostId(String postType, Long postId);
    List<Appointment> findByPostTypeAndPostId(String postType, Long postId);
    List<Appointment> findByBuyerIdAndPostType(Long buyerId, String postType);
    void deleteAllByPostTypeAndPostIdIn(String postType, List<Long> postIds);
}