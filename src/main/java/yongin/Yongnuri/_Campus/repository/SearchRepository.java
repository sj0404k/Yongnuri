package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yongin.Yongnuri._Campus.domain.Search;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Search,Long> {

    void deleteByUserId(Long id);

    List<Search> findAllByUserId(Long id);
}
