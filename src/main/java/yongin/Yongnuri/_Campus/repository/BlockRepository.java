package yongin.Yongnuri._Campus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import yongin.Yongnuri._Campus.domain.Block;
import yongin.Yongnuri._Campus.domain.User;

public interface BlockRepository extends JpaRepository<Block, Long> {
    
    // 차단유저정보
    List<Block> findByBlockerId(Long blockerId);

    Optional<Block> findByIdAndBlockedId(Long id, Long blockedId);

    Optional<Block> findByBlockerIdAndBlockedId(Long blocker, Long blockedId);

    List<Block> findAllByBlockedId(Long  blockedId);

    List<Block> findAllByBlockerId(Long id);
}