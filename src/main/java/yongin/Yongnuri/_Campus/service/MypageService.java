// yongin/Yongnuri/_Campus/service/MypageService.java
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));

        // 학번 우선순위:
        // 1) 엔티티의 studentId 필드 (숫자/문자 모두 허용) → 문자열 변환
        // 2) 없으면 email 로컬파트에서 숫자만 추출(임시 폴백)
        String studentId = resolveStudentId(user);

        // 닉네임: 엔티티에 이미 nickName 필드가 존재(setMypageDetails에서 사용중)
        String nickName = user.getNickName() != null ? user.getNickName() : "";

        return new MypageRes.getpage(
                studentId,
                safe(user.getName()),
                safe(user.getEmail()),
                nickName,
                user.getMajor()
        );
    }

    /** ✅ 내 정보 수정: 닉네임 변경 */
    public void setMypageDetails(String email, MypageReq.setpage req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        // 1. 필수값 확인
        if (Stream.of(req.getNickName()).anyMatch(s -> s == null || ((Constable) s).toString().isEmpty())) {
            throw new IllegalArgumentException("필수 데이터 미입력");
        }
        // 2. 수정된 부분 저장
        user.setNickName(req.getNickName());
        userRepository.save(user);
    }

    public boolean deleteBookMarks(String email, Long bookmarkId) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        Optional<BookMarks> bookmarkck = bookMarksRepository.findByUserIdAndId(currentUser.getId(), bookmarkId);

        if (bookmarkck.isPresent()) {
            bookMarksRepository.delete(bookmarkck.get());
            return true;
        } else {
            return false;
        }
    }

    public boolean postBlocks(String email, MypageReq.blocks mypageReq) {
        User targetUser = userRepository.findById(mypageReq.getBlockedId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "차단 대상 유저를 찾을 수 없습니다."));

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        Optional<Block> existing = blockRepository.findByBlockerIdAndBlockedId(currentUser.getId(),  targetUser.getId());
        if (existing.isPresent()) {
            return false; // 이미 차단된 상태
        }

        Block block = Block.builder()
                .blockerId(currentUser.getId())
                .blockedId(targetUser.getId())
                .createdDate(LocalDateTime.now())
                .build();

        blockRepository.save(block);
        return true;
    }

    public boolean deleteBlocks(String email, Long blockedId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        Optional<Block> blockOpt = blockRepository.findById(blockedId);

        if (blockOpt.isPresent()) {
            blockRepository.delete(blockOpt.get());
            return true;
        }
        return false;
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
                            .id(block.getId())
                            .blockedId(block.getBlockedId())
                            .blockedNickName(blockedUser.getNickName())
                            .build();
                })
                .toList();
    }

    // ---------- helpers ----------

    /** 학번 문자열 안전 추출 */
    private String resolveStudentId(User user) {
        // 1) 엔티티에서 직접 꺼내기 (필드명이 다를 수 있으니 필요 시 수정)
        try {
            // int/long 등 숫자 타입인 경우 문자열 변환
            Object stu = user.getStudentId(); // ← 엔티티에 studentId 필드가 있다고 가정
            if (stu != null) {
                String v = String.valueOf(stu);
                if (!v.isBlank()) return v;
            }
        } catch (NoSuchMethodError | NullPointerException ignored) {}

        // 2) 없으면 이메일 로컬파트에서 숫자만 추출(임시 폴백)
        String email = user.getEmail() != null ? user.getEmail() : "";
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String digits = local.replaceAll("\\D+", "");
        return digits; // 없으면 빈 문자열 반환
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
