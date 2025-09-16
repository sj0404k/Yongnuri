// security/JwtAuthenticationFilter.java
package yongin.Yongnuri._Campus.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import yongin.Yongnuri._Campus.repository.UserRepository;

import java.io.IOException;

// 이 필터가 우리가 찾던 '빠진 조각'입니다.
// OncePerRequestFilter: 모든 요청마다 딱 한 번만 실행되는 필터
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository; // (이메일로 User가 실존하는지 확인하기 위해)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 헤더에서 'Authorization' 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // "Bearer " 다음부터 토큰 추출
        }

        // 2. 토큰 유효성 검사
        if (token != null && jwtProvider.validateToken(token)) {
            // 3. 토큰에서 'email' 추출 (이게 Principal이 됨)
            String email = jwtProvider.getEmailFromToken(token);

            // (중요) DB에서 email로 User가 실존하는지 확인 (탈퇴한 유저 등)
            // (UserRepository에 existsByEmail 메서드가 있어야 합니다!)
            if (userRepository.existsByEmail(email)) { 
                
                // 4. 인증 객체 생성 (email을 Principal로 사용)
                // (우리가 찾던 가설 2번: Principal을 email 문자열로 저장)
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, null); // (권한은 일단 null)

                // 5. Spring Security 컨텍스트에 인증 정보 저장
                // 이제 @AuthenticationPrincipal String email 로 값을 꺼낼 수 있음
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}