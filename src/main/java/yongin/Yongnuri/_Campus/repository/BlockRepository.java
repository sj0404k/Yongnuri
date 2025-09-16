// repository/BlockRepository.java
package yongin.Yongnuri._Campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import yongin.Yongnuri._Campus.domain.Block;

public interface BlockRepository extends JpaRepository<Block, Long> {
    
    // BlockerId(나)를 기준으로 모든 Block 정보를 찾는 쿼리 메서드
    List<Block> findByBlockerId(Long blockerId);
}