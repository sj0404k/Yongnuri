// security/SecurityConfig.java
package yongin.Yongnuri._Campus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import yongin.Yongnuri._Campus.repository.UserRepository;

@Configuration
@EnableWebSecurity // <--- Spring Security 설정 클래스임을 선언
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository; // (필터가 UserRepository를 사용해야 함)

    // (🚨 중요) 비밀번호 암호화를 위한 Bean. JoinService에서도 이걸 사용해야 해요.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // (1) 기본 설정 비활성화
            .httpBasic(c -> c.disable()) // http basic auth 비활성화
            .csrf(c -> c.disable()) // csrf 비활성화
            .formLogin(c -> c.disable()) // form login 비활성화
            .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함 (JWT 사용)
            
            // (2) 엔드포인트별 권한 설정
            // (가정: 회원가입/로그인 API 경로는 /api/auth/** 라고 가정)
            .authorizeHttpRequests(auth -> auth
                // (수정) "/api/auth/**" -> "/auth/**"로 변경 (AuthController 경로와 일치)
                .requestMatchers("/auth/**").permitAll() 
                .anyRequest().authenticated() // 나머지는 인증 필요
            )
            
            // (3) 우리가 만들 JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtProvider, userRepository), // <--- 이 필터를 바로 아래에서 만듭시다!
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}