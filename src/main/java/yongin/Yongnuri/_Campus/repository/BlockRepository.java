package yongin.Yongnuri._Campus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import yongin.Yongnuri._Campus.domain.Block;

public interface BlockRepository extends JpaRepository<Block, Long> {
    
    // 차단유저정보
    List<Block> findByBlockerId(Long blockerId);
}