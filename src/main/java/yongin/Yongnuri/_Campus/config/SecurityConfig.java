package yongin.Yongnuri._Campus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 임포트 추가
import yongin.Yongnuri._Campus.security.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (JWT 사용 시 일반적으로)
                // .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 필요 시 CORS 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함 (JWT)
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 없이 접근 허용할 API 경로들
                        .requestMatchers("/auth/mail/**", "/auth/verify/**").permitAll()
                        .requestMatchers("/auth/**", "/auth/login").permitAll()
                        .requestMatchers("/mypage/**","/mypage/bookmarks").permitAll()
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                );

        // ⭐ JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 이전에 추가합니다. ⭐
        // 이렇게 하면 JWT를 통해 인증을 먼저 시도하고, 실패 시 다른 인증 메커니즘으로 넘어갈 수 있습니다.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}