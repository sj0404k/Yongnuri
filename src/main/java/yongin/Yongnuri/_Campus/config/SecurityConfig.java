//package yongin.Yongnuri._Campus.security; // Security 설정 클래스의 패키지 (예시)
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCryptPasswordEncoder
//import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity // Spring Security를 활성화
//@RequiredArgsConstructor
//public class SecurityConfig { // 또는 WebSecurityConfig
//
//    // kimsj님의 JwtProvider를 사용하는 JWT 인증 필터가 있다면 주입받아야 합니다.
//    // private final JwtAuthenticationFilter jwtAuthenticationFilter; // 예시: JWT 토큰을 검증하는 커스텀 필터
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(); // 비밀번호 해싱을 위한 빈 (이미 설정되어 있을 수도 있습니다)
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. CSRF (Cross-Site Request Forgery) 보호 비활성화 (JWT 사용 시 주로 비활성화)
//                .csrf(csrf -> csrf.disable())
//
//                // 2. CORS (Cross-Origin Resource Sharing) 설정 (필요에 따라 추가)
//                // .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                // 3. 세션 관리를 stateless (무상태)로 설정 (JWT 사용 시 세션을 사용하지 않음)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // 4. HTTP 요청에 대한 접근 권한 설정 (핵심!)
//                .authorizeHttpRequests(authorize -> authorize
//                        // 이메일 인증 API는 인증 없이 접근 허용 (이 부분이 중요해요!)
//                        // MailService가 Controller에서 어떤 경로로 매핑되는지 확인하여 정확히 지정해야 합니다.
//                        // 만약 MailService가 "/api/mail/**" 경로로 매핑된다면, 아래와 같이 작성합니다.
//                        .requestMatchers("/api/mail/**", "/mail/**").permitAll() // <-- 이메일 인증 관련 API 경로를 permitAll()
//
//                        // 로그인, 회원가입 등의 인증 관련 API도 인증 없이 접근 허용
//                        // 예: AuthController가 "/api/auth/**" 경로를 사용한다면
//                        .requestMatchers("/api/auth/**", "/register", "/login").permitAll()
//
//                        // H2 Console 같은 개발용 도구도 permitAll (개발 환경에서만)
//                        .requestMatchers("/h2-console/**").permitAll()
//
//                        // 그 외 모든 요청은 인증 필요
//                        .anyRequest().authenticated()
//                );
//
//        // 5. JWT 인증 필터 추가 (만약 JWT 필터가 있다면)
//        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // CORS 설정이 필요한 경우 추가 (예시)
//    /*
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 프런트엔드 URL
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
//        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//    */
//}