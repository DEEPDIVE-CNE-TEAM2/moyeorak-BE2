package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalCreateResponse {

    private Integer id;

    // Facility 정보
    private Long facilityId;               // ✅ 시설 ID
    private String facilityName;
    private String location;
    private String address;
    private String contact;                // ✅ 시설 연락처
    private String description;            // ✅ 시설 설명
    private String imageUrl;
    private Integer area;
    private Integer capacity;              // ✅ 시설 최대 수용 인원
    private LocalTime facilityUsageStartTime;  // ✅ 시설 운영 시작 시간
    private LocalTime facilityUsageEndTime;    // ✅ 시설 운영 종료 시간

    // Rental 정보
    private LocalDate usageStartDate;
    private LocalDate usageEndDate;
    private LocalTime usageStartTime;
    private LocalTime usageEndTime;

    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private LocalDate cancelEndDate;

    // Region 관리자 정보
    private String regionName;
    private String managerName;
    private String managerEmail;
}
