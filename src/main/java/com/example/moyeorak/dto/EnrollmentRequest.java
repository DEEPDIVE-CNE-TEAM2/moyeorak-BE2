package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {
    private String programTitle;   // 프로그램 제목
    private String place;          // facility.location
    private String usagePeriod;    // "YYYY-MM-DD ~ YYYY-MM-DD"
    private String usageTime;      // "HH:mm ~ HH:mm"
    private Integer paidAmount;
}