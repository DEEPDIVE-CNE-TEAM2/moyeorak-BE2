package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalResponse {

    private Integer id;

    // 시설 정보
    private Long facilityId;
    private String facilityName;
    private String location;
    private String address;
    private String imageUrl;
    private Integer area;

    // 대관 정보
    private LocalDate usageStartDate;
    private LocalDate usageEndDate;
    private LocalTime usageStartTime;
    private LocalTime usageEndTime;

    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private LocalDate cancelEndDate;

    private Integer capacity;
    private String contact;

    // 지역 정보
    private Long regionId;
}
