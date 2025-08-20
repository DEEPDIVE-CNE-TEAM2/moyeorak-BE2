// 요청이 오면 JWT 토큰 유효한지 확인 후 인증 처리
package com.example.moyeorak.jwt;

import com.example.moyeorak.repository.UserRepository;
import com.example.moyeorak.security.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
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
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        byte[] jsonBytes = new ObjectMapper()
                .writeValueAsBytes(errorResponse);

        response.getOutputStream().write(jsonBytes);
        response.getOutputStream().flush();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 프론트에서 CORS preflight 용도로 보내는 OPTIONS 요청은 그냥 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        // Authorization 헤더 가져오기
        String header = request.getHeader("Authorization");

        // Bearer 형식인지 검사
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 토큰 검증 try-catch
            try {
                // 💥 validateToken 제거 → 예외로 처리
                String email = jwtProvider.getEmail(token);  // 내부에서 파싱, 예외 발생 시 catch
                String role = jwtProvider.getRole(token);

                if (email != null && role != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

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
                        writeErrorResponse(response, ErrorCode.NOT_FOUND_USER);
                        return;
                    }
                }

            } catch (ExpiredJwtException e) {
                log.warn("❌ AccessToken 만료: {}", e.getMessage());
                writeErrorResponse(response, ErrorCode.EXPIRED_JWT);
                return;
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                log.warn("❌ 잘못된 JWT 서명 또는 토큰 형식 오류: {}", e.getMessage());
                writeErrorResponse(response, ErrorCode.INVALID_JWT);
                return;
            } catch (Exception e) {
                log.warn("❌ JWT 검증 중 기타 오류: {}", e.getMessage());
                writeErrorResponse(response, ErrorCode.JWT_AUTH_FAIL);
                return;
            }

        } else {
            log.warn("Authorization 헤더가 없거나 Bearer 형식이 아님: {}", header);
            writeErrorResponse(response, ErrorCode.INVALID_JWT);
            return;
        }

        // 위 조건들이 안 걸리면 요청을 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }
}
