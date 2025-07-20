package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminUserListResponseDto;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // 토큰 기반으로 관리자 식별 + 담당 지역 유저 조회
    public List<AdminUserListResponseDto> getUsersByRegionFromToken(HttpServletRequest request) {
        // 1. 토큰에서 관리자 이메일 꺼내기
        String token = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmail(token);

        // 2. 관리자 유저 조회
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalAccessError("관리자 권한이 없습니다.");
        }

        // 3. 관리자 담당 지역 확인
        Region region = admin.getRegion();
        if (region == null) {
            throw new IllegalStateException("관리자에게 지역 정보가 설정되어 있지 않습니다.");
        }

        // 4. 해당 지역의 일반 유저만 조회 (관리자는 제외)
        List<User> users = userRepository.findByRegionAndRole(region, User.Role.USER);

        // 5. DTO로 변환
        return users.stream()
                .map(user -> new AdminUserListResponseDto(
                        user.getId(),
                        user.getName(),
                        user.getGender().name(),
                        user.getEmail(),
                        user.getRegion().getName(),
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                ))
                .toList();
    }
}