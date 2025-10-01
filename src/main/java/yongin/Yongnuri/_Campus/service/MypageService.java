package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.domain.Block;
import yongin.Yongnuri._Campus.domain.BookMarks;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.BlocksRes;
import yongin.Yongnuri._Campus.dto.MypageReq;
import yongin.Yongnuri._Campus.dto.MypageRes;
import yongin.Yongnuri._Campus.repository.BlockRepository;
import yongin.Yongnuri._Campus.repository.BookMarksRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;

import java.lang.constant.Constable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final BookMarksRepository bookMarksRepository;
    private final BlockRepository blockRepository;

    public MypageRes.getpage getMypageDetails(String email) {
        System.out.println(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        return new MypageRes.getpage(user.getId(), user.getName(), user.getEmail());
    }


    public void setMypageDetails(String email, MypageReq.setpage req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        // 1. 필수값 확인
        if (Stream.of(req.getNickName())
                .anyMatch(s -> s == null || ((Constable) s).toString().isEmpty())) {
            throw new IllegalArgumentException("필수 데이터 미입력");
        }
        //2. 수정된 부분 저장
        user.setNickName(req.getNickName());
        userRepository.save(user);
    }

    public boolean deleteBookMarks(String email, Long bookmarkId) {
        //  차단한 유저(로그인한 유저) 찾기
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        // 북마크 존재 여부 확인
        Optional<BookMarks> bookmarkck = bookMarksRepository.findByUserIdAndId(currentUser.getId(), bookmarkId);

        if (bookmarkck.isPresent()) {
            bookMarksRepository.delete(bookmarkck.get());
            return true; // 삭제 성공
        } else {
            return false; // 삭제할 북마크 없음
        }
    }

    public boolean postBlocks(String email, MypageReq.blocks mypageReq) {
        // 1. 차단 대상 유저 찾기
        User targetUser = userRepository.findById(mypageReq.getBlockedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "차단 대상 유저를 찾을 수 없습니다."));

        // 2. 차단한 유저(로그인한 유저) 찾기
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        // 3. 이미 차단했는지 확인
        Optional<Block> existing = blockRepository.findByBlockerIdAndBlockedId(currentUser.getId(),  targetUser.getId());
        System.out.println("blockerId=" + currentUser.getId() + ", blockedId=" + targetUser.getId());
        System.out.println("existing.isPresent()=" + existing.isPresent());
        if (existing.isPresent()) {
            return false; // 이미 차단된 상태
        }
        // 4. 차단 저장
        Block block = Block.builder()
                .blockerId(currentUser.getId())        // 차단한 사람
                .blockedId(targetUser.getId())  // 차단당한 사람
                .createdDate(LocalDateTime.now())
                .build();

        blockRepository.save(block);
        return true; // 차단 성공
    }

    public boolean deleteBlocks(String email, Long blockedId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        Optional<Block> blockOpt = blockRepository.findById(blockedId);

        if (blockOpt.isPresent()) {
            blockRepository.delete(blockOpt.get());
            return true; // 삭제 성공
        }
        return false; // 삭제 실패 (존재하지 않음)
    }

    public List<BlocksRes> getBlocks(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        List<Block> blocks = blockRepository.findAllByBlockerId(currentUser.getId());

        return blocks.stream()
                .map(block -> {
                    User blockedUser = userRepository.findById(block.getBlockedId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "차단된 유저를 찾을 수 없습니다."));
                    return BlocksRes.builder()
                            .blockedId(block.getBlockedId())
                            .blockedNickName(blockedUser.getNickName())
                            .build();
                })
                .toList();
    }
}
