package yongin.Yongnuri._Campus.servise;


import yongin.Yongnuri._Campus.domain.Block;
import yongin.Yongnuri._Campus.repository.BlockRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;

    /**
     * 현재 유저가 차단한 유저 ID 목록을 반환합니다.
     * @param currentUserId (현재 로그인한 유저 ID)
     * @return 차단한 유저들의 ID 리스트
     */
    public List<Long> getBlockedUserIds(Long currentUserId) {
        // 1. DB에서 Block 목록 조회
        List<Block> blocks = blockRepository.findByBlockerId(currentUserId);
        
        // 2. ID 리스트로 변환 [10, 25, 42]
        return blocks.stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toList());
    }
}