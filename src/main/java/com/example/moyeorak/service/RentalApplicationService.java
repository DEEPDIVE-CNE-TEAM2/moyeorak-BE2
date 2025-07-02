package com.example.moyeorak.service;

import com.example.moyeorak.dto.RentalApplicationRequest;
import com.example.moyeorak.dto.RentalApplicationResponse;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.Rental;
import com.example.moyeorak.entity.RentalApplication;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.RentalApplicationRepository;
import com.example.moyeorak.repository.RentalRepository;
import com.example.moyeorak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalApplicationService {

    private final RentalApplicationRepository rentalApplicationRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final RegionRepository regionRepository;

    // 대관 신청 생성
    public RentalApplicationResponse createRentalApplication(RentalApplicationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("신청자가 존재하지 않습니다."));

        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new IllegalArgumentException("대관 공간이 존재하지 않습니다."));

        Region region = regionRepository.findById(Long.valueOf(request.getRegionId()))
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        RentalApplication application = RentalApplication.builder()
                .user(user)
                .rental(rental)
                .region(region)
                .requestedDate(request.getRequestedDate())
                .requestedStartTime(request.getRequestedStartTime())
                .requestedEndTime(request.getRequestedEndTime())
                .note(request.getNote())
                .status("approved")  // 기본 상태는 'approved'
                .build();

        RentalApplication saved = rentalApplicationRepository.save(application);

        return mapToResponse(saved);
    }

    // RentalApplication을 Response로 변환
    private RentalApplicationResponse mapToResponse(RentalApplication app) {
        return RentalApplicationResponse.builder()
                .id(app.getId())
                .userId(Math.toIntExact(app.getUser().getId()))      // Long으로 유지
                .rentalId(app.getRental().getId())
                .regionId(Math.toIntExact(app.getRegion().getId()))
                .requestedDate(app.getRequestedDate())
                .requestedStartTime(app.getRequestedStartTime())
                .requestedEndTime(app.getRequestedEndTime())
                .status(app.getStatus())
                .note(app.getNote())
                .createdAt(app.getCreatedAt())
                .build();
    }

    // 사용자가 자신이 신청한 대관을 취소하는 기능
    public RentalApplicationResponse cancelRentalApplication(Long applicationId, Long userId) {
        RentalApplication application = rentalApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("대관 신청이 존재하지 않습니다."));

        // 사용자가 자신이 신청한 대관만 취소 가능
        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("사용자는 자신이 신청한 대관만 취소할 수 있습니다.");
        }

        application.setStatus("cancelled");  // 상태를 'cancelled'로 변경
        RentalApplication updatedApplication = rentalApplicationRepository.save(application);

        return mapToResponse(updatedApplication);
    }

    // 사용자나 관리자가 대관 신청 상태를 'approved' 또는 'cancelled'로 변경할 수 있는 기능
    public RentalApplicationResponse updateApplicationStatus(Long applicationId, String status) {
        RentalApplication application = rentalApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 존재하지 않습니다."));

        if (!status.equals("approved") && !status.equals("cancelled")) {
            throw new IllegalArgumentException("상태는 'approved' 또는 'cancelled'만 가능합니다.");
        }

        application.setStatus(status);
        RentalApplication updated = rentalApplicationRepository.save(application);

        return mapToResponse(updated);
    }

    // 사용자가 신청한 모든 대관 신청 내역 조회
    public List<RentalApplicationResponse> getUserApplications(Long userId) {
        return rentalApplicationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 대관 신청 삭제 기능
    public String deleteRentalApplication(Long applicationId, Long userId) {
        RentalApplication application = rentalApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("대관 신청이 존재하지 않습니다."));

        // 사용자가 자신이 신청한 대관만 삭제 가능
        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("사용자는 자신이 신청한 대관만 삭제할 수 있습니다.");
        }

        rentalApplicationRepository.delete(application);  // 대관 신청 삭제

        return "대관 신청이 삭제되었습니다.";  // 삭제 완료 메시지 반환
    }

    // 모든 대관 신청 상태 조회
    public List<RentalApplicationResponse> getAllApplications() {
        return rentalApplicationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 특정 사용자 대관 신청 상태 조회
    public List<RentalApplicationResponse> getApplicationsByUser(Long userId) {
        return rentalApplicationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 특정 대관 공간에 대한 대관 신청 상태 조회
    public List<RentalApplicationResponse> getApplicationsByRental(Long rentalId) {
        // 대관 신청을 DB에서 조회
        List<RentalApplication> rentalApplications = rentalApplicationRepository.findByRentalId(rentalId);

        // 응답 DTO로 변환
        return rentalApplications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // RentalApplication -> RentalApplicationResponse 변환
    private RentalApplicationResponse convertToResponse(RentalApplication rentalApplication) {
        return new RentalApplicationResponse(
                rentalApplication.getId(),
                rentalApplication.getUser().getName(),
                rentalApplication.getStatus(),
                rentalApplication.getCreatedAt()
        );
    }
}
