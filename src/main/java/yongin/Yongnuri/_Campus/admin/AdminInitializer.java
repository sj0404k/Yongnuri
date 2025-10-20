package yongin.Yongnuri._Campus.admin;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import yongin.Yongnuri._Campus.domain.Enum;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.repository.UserRepository;

import java.time.LocalDateTime;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AdminConfig adminConfig;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository,
                            AdminConfig adminConfig,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminConfig = adminConfig;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByEmail(adminConfig.getEmail()).ifPresentOrElse(
                user -> {
                    // ✅ 이미 있으면 비번/권한 동기화
                    user.setPassword(passwordEncoder.encode(adminConfig.getPassword()));
                    user.setRole(adminConfig.getRole());
                    if (user.getNickName() == null || user.getNickName().isBlank()) {
                        user.setNickName(adminConfig.getNickName());
                    }
                    userRepository.save(user);
                    System.out.println("[ADMIN] 기존 관리자 계정 동기화 완료: " + user.getEmail());
                },
                () -> {
                    // ✅ 없으면 생성
                    User admin = User.builder()
                            .email(adminConfig.getEmail().trim())
                            .nickName(adminConfig.getNickName())
                            .password(passwordEncoder.encode(adminConfig.getPassword()))
                            .creatAt(LocalDateTime.now())
                            .status(Enum.authStatus.ACTIVE)
                            .role(adminConfig.getRole())
                            .build();
                    userRepository.save(admin);
                    System.out.println("[ADMIN] 관리자 계정 생성 완료: " + admin.getEmail());
                }
        );
    }
}
