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
                        //관리자
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 인증 없이 접근 허용할 API 경로들
                        .requestMatchers("/auth/mail/**", "/auth/verify/**").permitAll()
                        .requestMatchers("/auth/**", "/auth/login").permitAll()
                        //인증 필요? 나중에 작성 지금은 모두 허용중
                        .requestMatchers("/mypage/**","/mypage/bookmarks").permitAll()
                        .requestMatchers("/lost-items/**", "/used-items/**").permitAll()
                        .requestMatchers("/report/**").permitAll()          //마지 할때 권한 변경 필요4
                        .requestMatchers("/ws-stomp").permitAll()
                        .requestMatchers("/notice/**").permitAll()
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                );

        // ⭐ JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 이전에 추가합니다. ⭐
        // 이렇게 하면 JWT를 통해 인증을 먼저 시도하고, 실패 시 다른 인증 메커니즘으로 넘어갈 수 있습니다.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

/**
 //이미지  테스트용 인증 없는 코드
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
 import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 import yongin.Yongnuri._Campus.security.JwtAuthenticationFilter;
 import org.springframework.web.cors.CorsConfiguration;
 import org.springframework.web.cors.CorsConfigurationSource;
 import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

 @Configuration
 @EnableWebSecurity
 @RequiredArgsConstructor
 public class SecurityConfig {

 // JwtAuthenticationFilter를 생성자 주입으로 받도록 필드 추가
 private final JwtAuthenticationFilter jwtAuthenticationFilter;

 @Bean
 public PasswordEncoder passwordEncoder() {
 return new BCryptPasswordEncoder();
 }

 @Bean
 public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 http
 .csrf(csrf -> csrf.disable())
 .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <--- 1. CORS 설정 추가
 .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

 // (수정) 모든 요청("/**")을 인증 없이 허용하도록 변경
 .authorizeHttpRequests(authorize -> authorize
 .requestMatchers("/**").permitAll()
 );

 http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

 return http.build();
 }
 @Bean
 public CorsConfigurationSource corsConfigurationSource() {
 CorsConfiguration configuration = new CorsConfiguration();

 configuration.addAllowedOriginPattern("*"); // 모든 출처를 허용 (개발용)
 configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
 configuration.addAllowedHeader("*"); // 모든 헤더 허용
 configuration.setAllowCredentials(true); // 인증 정보 허용

 UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
 source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
 return source;
 }
 }
 */