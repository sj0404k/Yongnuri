package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yongin.Yongnuri._Campus.domain.GroupBuyApplicant;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkCountDto;

import java.util.List;


public interface GroupBuyApplicantRepository extends JpaRepository<GroupBuyApplicant, Long> {
    @Query("SELECT ga.postId as postId, COUNT(ga.id) as count FROM GroupBuyApplicant ga " +
            "WHERE ga.postId IN :postIds GROUP BY ga.postId")
    List<BookmarkCountDto> countByPostIdIn(@Param("postIds") List<Long> postIds);
    long countByPostId(Long postId);
    void deleteAllByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}