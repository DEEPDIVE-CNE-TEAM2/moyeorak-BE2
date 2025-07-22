package com.example.moyeorak.service;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.entity.*;
import com.example.moyeorak.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FacilityRepository facilityRepository;

    @Transactional
    public RentalCreateResponse createRental(RentalRequest request, String adminEmail) {
        Region region = regionRepository.findAll().stream()
                .filter(r -> r.getManager() != null && adminEmail.equals(r.getManager().getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("관리자가 담당하는 지역이 없습니다."));

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new IllegalArgumentException("해당 시설이 존재하지 않습니다."));

        Rental rental = Rental.builder()
                .region(region)
                .facility(facility)
                .category(request.getCategory())
                .target(request.getTarget())
                .usageStartDate(request.getUsageStartDate())
                .usageEndDate(request.getUsageEndDate())
                .usageStartTime(request.getUsageStartTime())
                .usageEndTime(request.getUsageEndTime())
                .registrationStartDate(request.getRegistrationStartDate())
                .registrationEndDate(request.getRegistrationEndDate())
                .cancelEndDate(request.getCancelEndDate())
                .capacity(request.getCapacity())
                .build();

        return mapToCreateResponse(rentalRepository.save(rental));
    }

    @Transactional
    public void deleteRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 대관 정보가 없습니다."));
        rentalRepository.delete(rental);
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
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대관 정보가 없습니다."));

        if (!rental.getRegion().getId().equals(regionId)) {
            throw new IllegalArgumentException("해당 지역에 속한 대관이 아닙니다.");
        }

        return mapToDetailResponse(rental);
    }

    public List<FacilityResponse> getFacilitiesByRegion(Long regionId) {
        return facilityRepository.findByRegionId(regionId).stream()
                .map(this::mapToFacilityResponse)
                .toList();
    }

    @Transactional
    public RentalCreateResponse partialUpdateRental(Long id, Map<String, Object> updates) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 대관 정보가 없습니다."));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "usageStartDate" -> rental.setUsageStartDate(LocalDate.parse(value.toString()));
                case "usageEndDate" -> rental.setUsageEndDate(LocalDate.parse(value.toString()));
                case "usageStartTime" -> rental.setUsageStartTime(LocalTime.parse(value.toString()));
                case "usageEndTime" -> rental.setUsageEndTime(LocalTime.parse(value.toString()));
                case "registrationStartDate" -> rental.setRegistrationStartDate(LocalDate.parse(value.toString()));
                case "registrationEndDate" -> rental.setRegistrationEndDate(LocalDate.parse(value.toString()));
                case "cancelEndDate" -> rental.setCancelEndDate(LocalDate.parse(value.toString()));
                case "capacity" -> rental.setCapacity(Integer.parseInt(value.toString()));
                case "facilityId" -> {
                    Long facilityId = Long.parseLong(value.toString());
                    Facility newFacility = facilityRepository.findById(facilityId)
                            .orElseThrow(() -> new IllegalArgumentException("시설을 찾을 수 없습니다."));
                    rental.setFacility(newFacility);
                }
                case "contact" -> {
                    Facility f = rental.getFacility();
                    f.setContact(value.toString());
                    facilityRepository.save(f);
                }
                case "description" -> {
                    Facility f = rental.getFacility();
                    f.setDescription(value.toString());
                    facilityRepository.save(f);
                }
            }
        }

        return mapToCreateResponse(rentalRepository.save(rental));
    }

    private RentalCreateResponse mapToCreateResponse(Rental rental) {
        User manager = rental.getRegion().getManager();
        Facility facility = rental.getFacility();

        return RentalCreateResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .regionName(rental.getRegion().getName())
                .managerName(manager != null ? manager.getName() : null)
                .managerEmail(manager != null ? manager.getEmail() : null)
                .facilityId(facility.getId())
                .facilityName(facility.getName())
                .location(facility.getLocation())
                .address(facility.getAddress())
                .contact(facility.getContact())
                .description(facility.getDescription())
                .imageUrl(facility.getImageUrl())
                .area(facility.getArea())
                .capacity(facility.getCapacity())
                .facilityUsageStartTime(facility.getUsageStartTime())
                .facilityUsageEndTime(facility.getUsageEndTime())
                .usageStartDate(rental.getUsageStartDate())
                .usageEndDate(rental.getUsageEndDate())
                .usageStartTime(rental.getUsageStartTime())
                .usageEndTime(rental.getUsageEndTime())
                .registrationStartDate(rental.getRegistrationStartDate())
                .registrationEndDate(rental.getRegistrationEndDate())
                .cancelEndDate(rental.getCancelEndDate())
                .build();
    }

    private RentalListResponse mapToListResponse(Rental rental) {
        Facility facility = rental.getFacility();

        return RentalListResponse.builder()
                .id(Math.toIntExact(rental.getId()))
                .location(facility.getLocation())
                .address(facility.getAddress())
                .usageTime(formatTimeRange(rental.getUsageStartTime(), rental.getUsageEndTime()))
                .capacity(rental.getCapacity())
                .imageUrl(facility.getImageUrl())
                .build();
    }

    private RentalDetailResponse mapToDetailResponse(Rental rental) {
        Facility facility = rental.getFacility();

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
                .location(facility.getLocation())
                .address(facility.getAddress())
                .usageTime(formatTimeRange(rental.getUsageStartTime(), rental.getUsageEndTime()))
                .registrationPeriod(formatDateRange(rental.getRegistrationStartDate(), rental.getRegistrationEndDate()))
                .cancelEndDate(rental.getCancelEndDate().toString())
                .capacity(rental.getCapacity())
                .contact(facility.getContact())
                .imageUrl(facility.getImageUrl())
                .reservedTimes(reservedTimes)
                .build();
    }

    private FacilityResponse mapToFacilityResponse(Facility facility) {
        return FacilityResponse.builder()
                .id(Math.toIntExact(facility.getId()))
                .location(facility.getLocation())
                .address(facility.getAddress())
                .capacity(facility.getCapacity())
                .usageTime(formatTimeRange(facility.getUsageStartTime(), facility.getUsageEndTime()))
                .imageUrl(facility.getImageUrl())
                .contact(facility.getContact())
                .build();
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }
}
