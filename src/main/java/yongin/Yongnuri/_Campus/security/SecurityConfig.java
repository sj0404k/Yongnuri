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
@EnableWebSecurity 
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository; 


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
           
            .httpBasic(c -> c.disable()) 
            .csrf(c -> c.disable())
            .formLogin(c -> c.disable()) 
            .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) 
            
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll() 
                .anyRequest().authenticated() 
            )
            
           
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtProvider, userRepository), 
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}