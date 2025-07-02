package com.example.moyeorak.service;

import com.example.moyeorak.dto.RentalRequest;
import com.example.moyeorak.dto.RentalResponse;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.Rental;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.RentalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RegionRepository regionRepository;

    public RentalResponse createRental(RentalRequest request) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        Rental rental = Rental.builder()
                .region(region)
                .category(request.getCategory())
                .location(request.getLocation())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .target(request.getTarget())
                .usageStartDate(request.getUsageStartDate())
                .usageEndDate(request.getUsageEndDate())
                .usageStartTime(request.getUsageStartTime())
                .usageEndTime(request.getUsageEndTime())
                .registrationStartDate(request.getRegistrationStartDate())
                .registrationEndDate(request.getRegistrationEndDate())
                .cancelEndDate(request.getCancelEndDate())
                .fee(request.getFee())
                .capacity(request.getCapacity())
                .contact(request.getContact())
                .build();

        Rental saved = rentalRepository.save(rental);

        return mapToResponse(saved);
    }

    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 상세 조회 추가 메서드
    public RentalResponse getRentalById(Long id) {
        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + "에 해당하는 대관 공간을 찾을 수 없습니다."));
        return mapToResponse(rental);
    }

    private RentalResponse mapToResponse(Rental rental) {
        return RentalResponse.builder()
                .id(rental.getId())
                .regionId(rental.getRegion().getId())
                .category(rental.getCategory())
                .location(rental.getLocation())
                .imageUrl(rental.getImageUrl())
                .description(rental.getDescription())
                .target(rental.getTarget())
                .usageStartDate(rental.getUsageStartDate())
                .usageEndDate(rental.getUsageEndDate())
                .usageStartTime(rental.getUsageStartTime())
                .usageEndTime(rental.getUsageEndTime())
                .registrationStartDate(rental.getRegistrationStartDate())
                .registrationEndDate(rental.getRegistrationEndDate())
                .cancelEndDate(rental.getCancelEndDate())
                .fee(rental.getFee())
                .capacity(rental.getCapacity())
                .contact(rental.getContact())
                .build();
    }

    @Transactional
    public RentalResponse updateRental(Long id, RentalRequest request) {
        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("ID " + id + "에 해당하는 대관 공간을 찾을 수 없습니다."));

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        rental.setRegion(region);
        rental.setCategory(request.getCategory());
        rental.setLocation(request.getLocation());
        rental.setImageUrl(request.getImageUrl());
        rental.setDescription(request.getDescription());
        rental.setTarget(request.getTarget());
        rental.setUsageStartDate(request.getUsageStartDate());
        rental.setUsageEndDate(request.getUsageEndDate());
        rental.setUsageStartTime(request.getUsageStartTime());
        rental.setUsageEndTime(request.getUsageEndTime());
        rental.setRegistrationStartDate(request.getRegistrationStartDate());
        rental.setRegistrationEndDate(request.getRegistrationEndDate());
        rental.setCancelEndDate(request.getCancelEndDate());
        rental.setFee(request.getFee());
        rental.setCapacity(request.getCapacity());
        rental.setContact(request.getContact());

        // 엔티티를 명시적으로 저장 (변경된 데이터를 반영)
        rentalRepository.save(rental);

        // 변경된 데이터를 반환
        return mapToResponse(rental);
    }

    public void deleteRental(Long id) {
        if (!rentalRepository.existsById(Math.toIntExact(id))) {
            throw new IllegalArgumentException("ID " + id + "에 해당하는 대관 공간을 찾을 수 없습니다.");
        }
        rentalRepository.deleteById(Math.toIntExact(id));

    }
}
