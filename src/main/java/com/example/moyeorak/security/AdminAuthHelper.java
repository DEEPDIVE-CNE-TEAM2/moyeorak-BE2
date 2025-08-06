package com.example.moyeorak.security;

import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuthHelper {

    private final UserRepository userRepository;

    public User getAdminFromRequest() {
        // 1. SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        // 2. 인증 객체에서 이메일(username) 가져오기
        String email = authentication.getName();

        // 3. DB 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        // 4. 권한 검증
        if (user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        return user;
    }
}