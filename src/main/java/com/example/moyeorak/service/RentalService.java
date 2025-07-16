package com.example.moyeorak.service;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.Rental;
import com.example.moyeorak.entity.RentalApplication;
import com.example.moyeorak.entity.RentalApplicationStatus;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.RentalApplicationRepository;
import com.example.moyeorak.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RegionRepository regionRepository;
    private final RentalApplicationRepository rentalApplicationRepository;

    @Transactional
    public RentalCreateResponse createRental(RentalRequest request, String adminEmail) {
        Region region = regionRepository.findAll().stream()
                .filter(r -> r.getManager() != null && adminEmail.equals(r.getManager().getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("관리자가 담당하는 지역이 없습니다."));

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
                .address(request.getAddress())
                .area(request.getArea())
                .build();

        return mapToCreateResponse(rentalRepository.save(rental));
    }

    @Transactional
    public RentalCreateResponse partialUpdateRental(Long id, Map<String, Object> updates) {
        Rental rental = rentalRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new IllegalArgumentException("대관 정보를 찾을 수 없습니다."));

        Set<String> allowedFields = Set.of(
                "regionId", "category", "location", "imageUrl", "description", "target",
                "usageStartDate", "usageEndDate", "usageStartTime", "usageEndTime",
                "registrationStartDate", "registrationEndDate", "cancelEndDate",
                "fee", "capacity", "contact", "address", "area"
        );

        updates.forEach((fieldName, value) -> {
            if (!allowedFields.contains(fieldName)) return;

            try {
                switch (fieldName) {
                    case "regionId" -> {
                        Region region = regionRepository.findById(Long.parseLong(value.toString()))
                                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));
                        rental.setRegion(region);
                    }
                    case "usageStartDate", "usageEndDate",
                         "registrationStartDate", "registrationEndDate", "cancelEndDate" -> {
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(rental, LocalDate.parse(value.toString()));
                    }
                    case "usageStartTime", "usageEndTime" -> {
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        field.set(rental, LocalTime.parse(value.toString()));
                    }
                    default -> {
                        Field field = Rental.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object parsed = field.getType().equals(Integer.class)
                                ? Integer.parseInt(value.toString())
                                : value;
                        field.set(rental, parsed);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("[" + fieldName + "] 필드 업데이트 실패: " + e.getMessage(), e);
            }
        });

        return mapToCreateResponse(rental);
    }

    @Transactional
    public void deleteRental(Long id) {
        if (!rentalRepository.existsById(Math.toIntExact(id))) {
            throw new IllegalArgumentException("해당 대관 정보가 없습니다.");
        }
        rentalRepository.deleteById(Math.toIntExact(id));
    }

    public List<RentalListResponse> getRentalsByManagerEmail(String email) {
        Region region = regionRepository.findAll().stream()
                .filter(r -> r.getManager() != null && email.equals(r.getManager().getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("관리자의 담당 지역이 없습니다."));

        return rentalRepository.findByRegionId(region.getId()).stream()
                .map(this::mapToListResponse)
                .toList();
    }

    public List<RentalListResponse> getRentalsByRegion(Long regionId) {
        return rentalRepository.findByRegionId(regionId).stream()
                .map(this::mapToListResponse)
                .toList();
    }

    public RentalDetailResponse getRentalDetailInRegion(Long regionId, Long rentalId) {
        Rental rental = rentalRepository.findById(Math.toIntExact(rentalId))
                .orElseThrow(() -> new IllegalArgumentException("해당 대관 정보가 없습니다."));

        if (!rental.getRegion().getId().equals(regionId)) {
            throw new IllegalArgumentException("해당 지역에 속한 대관이 아닙니다.");
        }

        return mapToDetailResponse(rental);
    }

    public List<FacilityResponse> getFacilitiesByRegion(Long regionId) {
        return rentalRepository.findByRegionId(regionId).stream()
                .map(this::mapToFacilityResponse)
                .toList();
    }

    private RentalCreateResponse mapToCreateResponse(Rental rental) {
        User manager = rental.getRegion().getManager();

        return RentalCreateResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .regionName(rental.getRegion().getName())
                .managerName(manager != null ? manager.getName() : null)
                .managerEmail(manager != null ? manager.getEmail() : null)
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
                .address(rental.getAddress())
                .area(rental.getArea())
                .build();
    }

    private RentalListResponse mapToListResponse(Rental rental) {
        return RentalListResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .location(rental.getLocation())
                .address(rental.getAddress())
                .usageTime(formatTimeRange(rental.getUsageStartTime(), rental.getUsageEndTime()))
                .capacity(rental.getCapacity())
                .imageUrl(rental.getImageUrl())
                .build();
    }

    private RentalDetailResponse mapToDetailResponse(Rental rental) {
        List<RentalApplication> applications = rentalApplicationRepository.findByRentalId(rental.getId());

        Map<LocalDate, List<TimeRange>> reservedTimes = applications.stream()
                .filter(app -> app.getStatus() == RentalApplicationStatus.APPROVED)
                .collect(Collectors.groupingBy(
                        RentalApplication::getRequestedDate,
                        Collectors.mapping(app -> TimeRange.builder()
                                .startTime(app.getRequestedStartTime())
                                .endTime(app.getRequestedEndTime())
                                .build(), Collectors.toList())
                ));

        return RentalDetailResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .category(rental.getCategory())
                .location(rental.getLocation())
                .address(rental.getAddress())
                .usageTime(formatTimeRange(rental.getUsageStartTime(), rental.getUsageEndTime()))
                .registrationPeriod(formatDateRange(rental.getRegistrationStartDate(), rental.getRegistrationEndDate()))
                .cancelEndDate(rental.getCancelEndDate().toString())
                .capacity(rental.getCapacity())
                .contact(rental.getContact())
                .imageUrl(rental.getImageUrl())
                .reservedTimes(reservedTimes)
                .build();
    }

    private FacilityResponse mapToFacilityResponse(Rental rental) {
        return FacilityResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .location(rental.getLocation())
                .address(rental.getAddress())
                .area(rental.getArea())
                .usageTime(formatTimeRange(rental.getUsageStartTime(), rental.getUsageEndTime()))
                .imageUrl(rental.getImageUrl())
                .contact(rental.getContact())
                .build();
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }
}