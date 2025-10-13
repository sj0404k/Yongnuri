package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yongin.Yongnuri._Campus.domain.Image;
import java.util.List;
public interface ImageRepository extends JpaRepository<Image, Long> {

    // 이미지전체삭제
    void deleteAllByTypeAndTypeId(String type, Long typeId);

    List<Image> findByTypeAndTypeIdOrderBySequenceAsc(String type, Long typeId);
    List<Image> findByTypeAndTypeIdInAndSequence(String type, List<Long> typeIds, int sequence);
    boolean existsByImageUrl(String imageUrl);
}