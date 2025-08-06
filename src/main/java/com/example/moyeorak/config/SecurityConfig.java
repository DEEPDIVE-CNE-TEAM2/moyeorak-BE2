package com.example.moyeorak.config;

import com.example.moyeorak.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/actuator/health", "/actuator/info", "/health",
                                "/api/users/signup", "/api/users/login",
                                "/api/users/check-email", "/api/users/check-phone",
                                "/api/regions/**",
                                "/api/programs/region/**",
                                "/api/users/refresh",
                                "/api/rentals/region/**",
                                "/api/rentals/facilities/region/**",
                                "/api/programs", "/api/programs/{id}",
                                "/api/facilities/{id:[\\d]+}",
                                "/api/facilities/region/{regionId:[\\d]+}",
                                "/api/main-images/region/**",
                                "/api/notices/region/**",
                                "/api/notices/{id}",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패 시
                            log.warn("[AUTH FAIL] URI: {}, Method: {}, Message: {}",
                                    request.getRequestURI(),
                                    request.getMethod(),
                                    authException.getMessage());

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    String.format(
                                            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                                            authException.getMessage(),
                                            request.getRequestURI()
                                    )
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 권한 부족 시
                            log.warn("[ACCESS DENIED] URI: {}, Method: {}, Auth: {}",
                                    request.getRequestURI(),
                                    request.getMethod(),
                                    SecurityContextHolder.getContext().getAuthentication());

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    String.format(
                                            "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\"}",
                                            accessDeniedException.getMessage(),
                                            request.getRequestURI()
                                    )
                            );
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://goorm-alb-1610121085.ap-northeast-2.elb.amazonaws.com",
                "https://www.moyeorak.cloud",
                "https://moyeorak.cloud",
                "https://api.moyeorak.cloud"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1시간 preflight 캐시

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
