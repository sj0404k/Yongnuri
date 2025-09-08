package yongin.Yongnuri._Campus.servise;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import yongin.Yongnuri._Campus.domain.RefreshToken;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.AuthReq;
import yongin.Yongnuri._Campus.exception.ConflictException;
import yongin.Yongnuri._Campus.repository.RefreshTokenRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;
import yongin.Yongnuri._Campus.security.JwtProvider;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final MailService mailService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public void join(AuthReq.joinReqDto req) {
        // 1. 필수값 확인
        if (Stream.of(req.getEmail(), req.getPassword(), req.getPasswordCheck(),
                        req.getName(), req.getMajor(), req.getNickname())
                .anyMatch(s -> s == null || s.trim().isEmpty())) {
            throw new IllegalArgumentException("필수 데이터 미입력");
        }

        // 1.1 이미 회원으로 존재한는 이메일인지 확인 ////---   혹시 몰라서 일단 넣어둠
//        if (userRepository.existsByEmail((req.getEmail()))) {
//            throw new ConflictException("이미 가입된 이메일");
//        }

        // 2. 이메일 인증 여부 확인
        if (!mailService.isVerified(req.getEmail())) {
            throw new SecurityException("이메일 인증 실패");
        }

        // 3. 비밀번호 일치 여부 확인
        if (!req.getPassword().equals(req.getPasswordCheck())) {
            throw new SecurityException("비밀번호 불일치");
        }

        // 4. 비밀번호 유효성 검사
        if (!PasswordValidator(req.getPassword())) {
            throw new IllegalArgumentException("비밀번호 형식 불일치");
        }

        // 5. 닉네임 중복 확인
        if (userRepository.existsByNickName((req.getNickname()))) {
            throw new ConflictException("이미 존재하는 닉네임");
        }

        // 6. 회원 저장
        User newUser = User.builder()
                .email(req.getEmail())
                .password(req.getPassword()) // 나중에 반드시 암호화 처리 필요함 !!! 해시처리 완료시 주석 지우기!!
                .name(req.getName())
                .major(req.getMajor())
                .nickName(req.getNickname())
                .build();

        userRepository.save(newUser);
    }

    // 비밀번호 유효성 검사 // 조건 추가 필욯함!!!!!!!!!!
    public static boolean PasswordValidator(String password) {
        // 최소 8자 + 특수문자 1개 이상
        return password.matches("^(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$");
    }


    // 로그인
    public ResponseEntity<String> login(String email, String password) {

        // 1. 유효성 확인
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("데이터 미입력");
        }

        // 2. 이메일로 회원정보 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("가입되지 않은 이메일"));

        // 3. 비밀번호 검증
        // (비밀번호 암호화를 했다면 PasswordEncoder.matches()로 검증)
        if (!user.getPassword().equals(password)) {
            throw new SecurityException("비밀번호 불일치");
        }

        // 4. 토큰 발급 (JWT 발급)
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), "User");
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // Refresh Token 저장 (DB or Redis에 저장)
        refreshTokenRepository.save(
                new RefreshToken(user.getEmail(), refreshToken)
        );

        // 5. 응답 반환 refreshtoken 나중에 지울것 !!
        String responseBody = String.format("""
    {
        "message": "로그인 성공",
        "accessToken": "%s",
        "refreshToken": "%s"
    }
    """, accessToken, refreshToken);

        return ResponseEntity.ok(responseBody);
    }

}
