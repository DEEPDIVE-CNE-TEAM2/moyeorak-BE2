package com.example.moyeorak.service;

import com.example.moyeorak.dto.FacilityDto;
import com.example.moyeorak.dto.FacilitySimpleDto;
import com.example.moyeorak.dto.FacilityDetailDto;
import com.example.moyeorak.dto.FacilityUpdateDto;
import com.example.moyeorak.entity.Facility;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.repository.FacilityRepository;
import com.example.moyeorak.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final RegionRepository regionRepository;

    // CloudFront 도메인 (application.properties에서 주입)
    @Value("${cloudfront.base-url}")
    private String cloudFrontBaseUrl;

    // 과거/입력으로 들어올 수 있는 S3 URL 식별용
    private static final String S3_HOST_PREFIX = "https://s3-goorm-frontend.s3.ap-northeast-2.amazonaws.com/";

    private String formatUsageTime(Facility facility) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return facility.getUsageStartTime().format(formatter) + " ~ " + facility.getUsageEndTime().format(formatter);
    }

    /**
     * 들어온 값(파일명 혹은 절대 URL)을 '파일명'만 남기도록 정규화
     * - S3 전체 URL, CloudFront 전체 URL 모두 대응
     */
    private String normalizeToFileName(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String val = raw.trim();

        // 절대 URL이면 마지막 "/" 뒤만 추출
        if (val.startsWith("http://") || val.startsWith("https://")) {
            return val.substring(val.lastIndexOf('/') + 1);
        }
        // 이미 파일명이면 그대로 반환
        return val;
    }

    /**
     * 응답용 절대경로: CloudFront 베이스 + 파일명
     * (들어온 값이 절대 URL이든 파일명이든 결과는 CloudFront 절대경로로 통일)
     */
    private String toCloudFrontUrl(String fileNameOrUrl) {
        if (fileNameOrUrl == null || fileNameOrUrl.isBlank()) return null;
        String fileName = normalizeToFileName(fileNameOrUrl);
        // cloudFrontBaseUrl이 /로 끝나지 않는다고 가정
        return cloudFrontBaseUrl + "/" + fileName;
    }

    @Transactional
    public FacilityDto createFacility(FacilityDto dto) {
        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        Facility facility = Facility.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .location(dto.getLocation())
                .contact(dto.getContact())
                // 저장은 파일명만 (절대 URL이 들어와도 파일명으로 정규화)
                .imageUrl(normalizeToFileName(dto.getImageUrl()))
                .capacity(dto.getCapacity())
                .description(dto.getDescription())
                .area(dto.getArea())
                .region(region)
                .usageStartTime(dto.getUsageStartTime())
                .usageEndTime(dto.getUsageEndTime())
                .build();

        Facility saved = facilityRepository.save(facility);

        // 응답은 CloudFront 절대경로로 내려줌
        return FacilityDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .address(saved.getAddress())
                .location(saved.getLocation())
                .contact(saved.getContact())
                .imageUrl(toCloudFrontUrl(saved.getImageUrl()))
                .capacity(saved.getCapacity())
                .description(saved.getDescription())
                .regionId(saved.getRegion().getId())
                .usageStartTime(saved.getUsageStartTime())
                .usageEndTime(saved.getUsageEndTime())
                .area(saved.getArea())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FacilitySimpleDto> getFacilitiesByRegion(Long regionId) {
        return facilityRepository.findByRegionId(regionId).stream()
                .map(facility -> FacilitySimpleDto.builder()
                        .id(facility.getId())
                        .location(facility.getName())
                        .address(facility.getAddress())
                        .usageTime(formatUsageTime(facility))
                        .contact(facility.getContact())
                        // 항상 CloudFront 절대경로로 응답
                        .imageUrl(toCloudFrontUrl(facility.getImageUrl()))
                        .area(facility.getArea())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacilityDetailDto getFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        return FacilityDetailDto.builder()
                .id(facility.getId())
                .location(facility.getName())
                .address(facility.getAddress())
                .usageTime(formatUsageTime(facility))
                .capacity(facility.getCapacity())
                .imageUrl(toCloudFrontUrl(facility.getImageUrl()))
                .description(facility.getDescription())
                .contact(facility.getContact())
                .build();
    }

    @Transactional
    public FacilityDto updateFacility(Long id, FacilityUpdateDto dto) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        if (dto.getName() != null) facility.setName(dto.getName());
        if (dto.getAddress() != null) facility.setAddress(dto.getAddress());
        if (dto.getContact() != null) facility.setContact(dto.getContact());
        if (dto.getImageUrl() != null) {
            // 업데이트 시에도 DB에는 파일명만 저장
            facility.setImageUrl(normalizeToFileName(dto.getImageUrl()));
        }
        if (dto.getCapacity() != null) facility.setCapacity(dto.getCapacity());
        if (dto.getDescription() != null) facility.setDescription(dto.getDescription());
        if (dto.getLocation() != null) facility.setLocation(dto.getLocation());
        if (dto.getArea() != null) facility.setArea(dto.getArea());

        if (dto.getUsageStartTime() != null)
            facility.setUsageStartTime(LocalTime.parse(dto.getUsageStartTime()));
        if (dto.getUsageEndTime() != null)
            facility.setUsageEndTime(LocalTime.parse(dto.getUsageEndTime()));

        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(Long.valueOf(dto.getRegionId()))
                    .orElseThrow(() -> new IllegalArgumentException("Region not found"));
            facility.setRegion(region);
        }

        // 응답은 CloudFront 절대경로로 내려줌
        return FacilityDto.builder()
                .id(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .contact(facility.getContact())
                .imageUrl(toCloudFrontUrl(facility.getImageUrl()))
                .capacity(facility.getCapacity())
                .description(facility.getDescription())
                .regionId(facility.getRegion().getId())
                .location(facility.getLocation())
                .usageStartTime(facility.getUsageStartTime())
                .usageEndTime(facility.getUsageEndTime())
                .area(facility.getArea())
                .build();
    }

    @Transactional
    public void deleteFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
        facilityRepository.delete(facility);
    }
}
