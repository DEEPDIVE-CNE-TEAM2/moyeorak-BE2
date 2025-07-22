package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalDetailResponse {

    private Integer id;

    // 시설 정보
    private String location;
    private String address;
    private String imageUrl;

    // 대관 정보
    private String usageTime;
    private String registrationPeriod;
    private String cancelEndDate;
    private Integer capacity;
    private String contact;

    // 예약 현황
    private Map<LocalDate, List<TimeRange>> reservedTimes;
}
