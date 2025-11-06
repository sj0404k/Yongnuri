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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yongin.Yongnuri._Campus.security.JwtAuthenticationFilter;
import org.springframework.security.config.Customizer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
                .csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
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
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 관리자
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // deleteAccount 인증 필요
                        .requestMatchers(HttpMethod.POST, "/auth/deleteAccount").authenticated()

                        // 공지사항
                        .requestMatchers(HttpMethod.GET, "/board/notices/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/board/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/board/notices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/board/notices/**").hasRole("ADMIN")

                        // 정적 업로드 이미지
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                        // 인증 없이 접근 허용할 auth API
                        .requestMatchers("/auth/email/**", "/auth/verify/**").permitAll()
                        .requestMatchers("/auth/resetPassword").permitAll()
                        .requestMatchers("/auth/join").permitAll()
                        .requestMatchers("/auth/login").permitAll()

                        // 채팅(테스트용)
                        .requestMatchers("/chat/**").permitAll()

                        // 마이페이지
                        .requestMatchers("/mypage/**", "/mypage/bookmarks").authenticated()

                        // 기타 공개 API
                        .requestMatchers("/ws-stomp/**", "/ws-stomp").permitAll()
                        .requestMatchers("/lost-items/**", "/used-items/**").permitAll()
                        .requestMatchers("/notifications/**").permitAll()
                        .requestMatchers("/report/**").permitAll()
                        .requestMatchers("/Yongnuri.apk").permitAll()  // APK 다운로드 허용

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 모든 도메인에서 접근 가능하도록 CORS 설정 (테스트용)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*") // 실제 서비스 시 도메인 제한 필요
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}
