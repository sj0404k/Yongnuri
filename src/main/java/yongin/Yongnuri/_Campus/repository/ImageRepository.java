// repository/ImageRepository.java
package yongin.Yongnuri._Campus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import yongin.Yongnuri._Campus.domain.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // (추가) 타입과 타입 ID로 모든 이미지 삭제 (예: "USED_ITEM" 타입, 15번 게시글의 모든 이미지 삭제)
    void deleteAllByTypeAndTypeId(String type, Long typeId);
}