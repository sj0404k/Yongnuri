package yongin.Yongnuri._Campus.servise;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.MypageReq;
import yongin.Yongnuri._Campus.dto.MypageRes;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.CustomUserDetails;

import java.lang.constant.Constable;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class MypageService {
    private final UserRepository userRepository;

    public MypageRes.getpage getMypageDetails(@AuthenticationPrincipal CustomUserDetails token) {
        System.out.println(token.getUser().getRole());
        User user = userRepository.findByEmail(token.getUser().getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        return new MypageRes.getpage(user.getId(), user.getName(), user.getEmail());
    }


    public MypageReq.setpage setMypageDetails(String token, MypageReq.setpage res) {
        User user = userRepository.findByEmail(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        // 1. 필수값 확인
        if (Stream.of(res.getEmail(), res.getStudentId(), res.getNickName())
                .anyMatch(s -> s == null || ((Constable) s).toString().isEmpty())) {
            throw new IllegalArgumentException("필수 데이터 미입력");
        }
        User newUser = User.builder()
                .email(res.getEmail())
                .studentId(res.getStudentId())
                .nickName(res.getNickName())
                .build();
        userRepository.save(newUser);
        return res;
    }
}
