package com.example.moyeorak.jwt;

import com.example.moyeorak.repository.UserRepository;
import com.example.moyeorak.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmail(token);
                String role = jwtProvider.getRole(token);

                if (email != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        String authority = "ROLE_" + role.toUpperCase();
                        var authorities = List.of(new SimpleGrantedAuthority(authority));

                        var userDetails = new CustomUserDetails(
                                user.getId(),
                                user.getEmail(),
                                user.getPassword(),
                                authorities
                        );

                        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        log.debug("✅ JWT 인증 성공 - 사용자: {}, 권한: {}", email, authority);
                    } else {
                        log.warn("❌ 사용자 DB 조회 실패 - email: {}", email);
                    }
                }
            } else {
                log.warn("❌ 유효하지 않은 JWT 토큰: {}", token);
            }
        } else {
            log.debug("ℹ️ Authorization 헤더 없음 또는 Bearer 토큰 형식 아님");
        }

        filterChain.doFilter(request, response);
    }
}
