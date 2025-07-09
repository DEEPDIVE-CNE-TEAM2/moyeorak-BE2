package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.service.RentalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@Slf4j
public class RentalController {

    private final RentalService rentalService;
    private final JwtProvider jwtProvider;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalCreateResponse> createRental(
            @RequestBody @Valid RentalRequest request,
            HttpServletRequest servletRequest
    ) {
        String token = servletRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        String email = jwtProvider.getEmail(token.substring(7));
        log.info("[POST] 대관 등록 요청 by 관리자: {}", email);

        RentalCreateResponse created = rentalService.createRental(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ✅ 관리자 담당 지역의 대관 목록만 조회
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalListResponse>> getRentalsByAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        String email = jwtProvider.getEmail(token.substring(7));
        log.info("[GET] 관리자 자기 지역 대관 목록 조회 - 관리자: {}", email);

        return ResponseEntity.ok(rentalService.getRentalsByManagerEmail(email));
    }

    // ✅ 지역별 대관 목록 조회 (사용자용)
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<RentalListResponse>> getRentalsByRegion(@PathVariable Long regionId) {
        log.info("[GET] 지역별 대관 조회 요청 - Region ID: {}", regionId);
        return ResponseEntity.ok(rentalService.getRentalsByRegion(regionId));
    }

    // ✅ 지역별 대관 상세 조회
    @GetMapping("/region/{regionId}/{rentalId}")
    public ResponseEntity<RentalDetailResponse> getRentalDetailByRegion(
            @PathVariable Long regionId,
            @PathVariable Long rentalId) {
        log.info("[GET] 지역 {}의 대관 상세 조회 - Rental ID: {}", regionId, rentalId);
        return ResponseEntity.ok(rentalService.getRentalDetailInRegion(regionId, rentalId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalCreateResponse> patchRental(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        log.info("[PATCH] 대관 수정 요청 - ID: {}, 업데이트 필드: {}", id, updates);
        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        RentalCreateResponse updated = rentalService.partialUpdateRental(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteRental(@PathVariable Long id) {
        log.info("[DELETE] 대관 삭제 요청 - ID: {}", id);
        rentalService.deleteRental(id);
        return ResponseEntity.ok(new MessageResponse("삭제되었습니다."));
    }

    // ✅ 지역별 시설 목록 조회 (면적 포함)
    @GetMapping("/facilities/region/{regionId}")
    public ResponseEntity<List<FacilityResponse>> getFacilitiesByRegion(@PathVariable Long regionId) {
        log.info("[GET] 지역별 시설 조회 요청 - Region ID: {}", regionId);
        return ResponseEntity.ok(rentalService.getFacilitiesByRegion(regionId));
    }
}
