package yongin.Yongnuri._Campus.service;


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
     * 차단한 유저 ID 목록을 반환합니다.
     * @param currentUserId 
     * @return 
     */
    public List<Long> getBlockedUserIds(Long currentUserId) {
        List<Block> blocks = blockRepository.findByBlockerId(currentUserId);

        return blocks.stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toList());
    }
}