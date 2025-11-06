package yongin.Yongnuri._Campus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yongin.Yongnuri._Campus.security.JwtAuthenticationFilter;
import org.springframework.security.config.Customizer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // 1. CORS 설정을 활성화합니다.
                .cors(Customizer.withDefaults())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType("application/json;charset=UTF-8");
                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("timestamp", LocalDateTime.now().toString());
                        body.put("status", HttpStatus.FORBIDDEN.value());
                        body.put("error", "Forbidden");
                        body.put("message", "접근 권한이 없습니다.");
                        body.put("path", request.getRequestURI());
                        new ObjectMapper().writeValue(response.getWriter(), body);
                    });
                    exception.authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json;charset=UTF-8");
                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("timestamp", LocalDateTime.now().toString());
                        body.put("status", HttpStatus.UNAUTHORIZED.value());
                        body.put("error", "Unauthorized");
                        body.put("message", "인증이 필요합니다. 로그인을 진행해주세요.");
                        body.put("path", request.getRequestURI());
                        new ObjectMapper().writeValue(response.getWriter(), body);
                    });
                })
                .authorizeHttpRequests(authorize -> authorize
                        // 1. 모든 OPTIONS 요청을 허용합니다. (CORS 사전 요청)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. 인증 없이 접근 허용 (PermitAll)
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/auth/email/**", "/auth/verify/**", "/auth/resetPassword", "/auth/join", "/auth/login").permitAll()
                        .requestMatchers("/search").permitAll()
                        .requestMatchers("/ws/chat/**").permitAll()
                        .requestMatchers("/notifications/**").permitAll()
                        .requestMatchers("/report/**").permitAll()
                        .requestMatchers("/Yongnuri.apk").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/chat/**").permitAll() // 모든 채팅 경로는 현재 permitAll로 설정되어 있음
                        // 게시판 목록 조회 (읽기 전용)
                        .requestMatchers(HttpMethod.GET, "/lost-items/**", "/used-items/**").permitAll()

                        // 3. 관리자 권한 필요 (ADMIN)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 공지사항 작성/수정/삭제
                        .requestMatchers(HttpMethod.POST, "/board/notices", "/board/notices/allnotice").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/board/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/board/notices/**").hasRole("ADMIN")

                        // 4. 인증 필요 (Authenticated)
                        // 인증/마이페이지
                        .requestMatchers(HttpMethod.POST, "/auth/deleteAccount").authenticated()
                        .requestMatchers("/mypage/**", "/mypage/bookmarks").authenticated()
                        // 검색 기록
                        .requestMatchers("/search/history/**", "/search/test").authenticated()
                        // 거래/히스토리
                        .requestMatchers("/history/**").authenticated() // 내역 조회
                        .requestMatchers("/board/makedeal/**").authenticated() // 약속 생성/수정
                        .requestMatchers("/board/delete-post").authenticated() // 통합 게시글 삭제
                        .requestMatchers("/board/bookmarks").authenticated() // 북마크 생성/삭제/조회
                        // 게시글 작성/수정
                        .requestMatchers(HttpMethod.POST, "/board/lost-found", "/board/market").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/board/lost-found/**", "/board/market/**").authenticated()
                        .requestMatchers("/board/group-buys/**").authenticated()
                        // 공지사항 조회
                        .requestMatchers(HttpMethod.GET, "/board/notices/**", "/board/notices/allnoticedetail/**", "/board/notices/allnotice").authenticated()

                        // 5. 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}