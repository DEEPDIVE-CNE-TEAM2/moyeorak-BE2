package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminUserListResponseDto;
import com.example.moyeorak.service.admin.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ✅ AccessToken 기반으로 관리자 유저 식별
    @GetMapping
    public List<AdminUserListResponseDto> getUsersByRegion(HttpServletRequest request) {
        return adminUserService.getUsersByRegionFromToken(request);
    }
}