package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yongin.Yongnuri._Campus.domain.Bookmark;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yongin.Yongnuri._Campus.dto.bookmark.BookmarkCountDto ;
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndPostTypeAndPostId(Long userId, String postType, Long postId);

    void deleteByUserIdAndPostTypeAndPostId(Long userId, String postType, Long postId);

    List<Bookmark> findByUserIdAndPostTypeAndPostIdIn(Long userId, String postType, List<Long> postIds);

    @Query("SELECT b.postId as postId, COUNT(b.id) as count FROM Bookmark b " +
            "WHERE b.postType = :postType AND b.postId IN :postIds GROUP BY b.postId")
    List<BookmarkCountDto> countByPostTypeAndPostIdIn(@Param("postType") String postType, @Param("postIds") List<Long> postIds);
    long countByPostTypeAndPostId(String postType, Long postId);
    void deleteAllByPostTypeAndPostId(String postType, Long postId);
    List<Bookmark> findByUserIdAndPostTypeOrderByCreatedAtDesc(Long userId, String postType);
}
