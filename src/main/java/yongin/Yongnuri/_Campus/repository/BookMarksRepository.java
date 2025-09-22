package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.BookMarks;

import java.util.Optional;

@Repository
public interface BookMarksRepository extends JpaRepository<BookMarks, Long> {
    Optional<BookMarks> findByUserIdAndId(Long userId, Long id);
}
