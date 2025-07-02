package com.example.moyeorak.controller;

import com.example.moyeorak.dto.RentalApplicationRequest;
import com.example.moyeorak.dto.RentalApplicationResponse;
import com.example.moyeorak.dto.RentalApplicationStatusUpdateRequest;
import com.example.moyeorak.service.RentalApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental-applications")
@RequiredArgsConstructor
public class RentalApplicationController {

    private final RentalApplicationService rentalApplicationService;

    // 대관 신청 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RentalApplicationResponse> createRentalApplication(@RequestBody @Valid RentalApplicationRequest request) {
        RentalApplicationResponse response = rentalApplicationService.createRentalApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 대관 신청 조회
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getMyApplications(@AuthenticationPrincipal(expression = "id") Long userId) {
        List<RentalApplicationResponse> responses = rentalApplicationService.getUserApplications(userId);
        return ResponseEntity.ok(responses);
    }

    // 특정 사용자 대관 신청 상태 조회 (관리자용)
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getUserApplications(@PathVariable Long userId) {
        List<RentalApplicationResponse> responses = rentalApplicationService.getUserApplications(userId);
        return ResponseEntity.ok(responses);
    }

    // 대관 신청 상태 업데이트 (관리자용)
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody @Valid RentalApplicationStatusUpdateRequest request) {

        RentalApplicationResponse response = rentalApplicationService.updateApplicationStatus(applicationId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    // 사용자가 대관 신청을 취소하는 API
    @DeleteMapping("/{applicationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> cancelRentalApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal(expression = "id") Long userId) {

        String message = rentalApplicationService.deleteRentalApplication(applicationId, userId);  // 삭제 후 메시지 반환
        return ResponseEntity.ok(message);  // 삭제 완료 메시지를 반환
    }

    // 모든 대관 신청 상태 조회 (관리자용)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getAllApplications() {
        List<RentalApplicationResponse> responses = rentalApplicationService.getAllApplications();
        return ResponseEntity.ok(responses);
    }

    // 특정 사용자의 대관 신청 상태 조회 (관리자용)
    @GetMapping("/user-applications/{userId}")  // 경로 수정
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getApplicationsByUser(@PathVariable Long userId) {
        List<RentalApplicationResponse> responses = rentalApplicationService.getApplicationsByUser(userId);
        return ResponseEntity.ok(responses);
    }

    // 특정 대관 공간에 대한 대관 신청 상태 조회 (관리자용)
    @GetMapping("/rental/{rentalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getApplicationsByRental(@PathVariable Long rentalId) {
        List<RentalApplicationResponse> responses = rentalApplicationService.getApplicationsByRental(rentalId);
        return ResponseEntity.ok(responses);
    }
}
