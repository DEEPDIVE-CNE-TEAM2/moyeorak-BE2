package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalDetailResponse {
    private Integer id;
    private String category;
    private String location;
    private String address;
    private String usageTime;
    private String registrationPeriod;
    private String cancelEndDate;
    private Integer capacity;
    private String contact;
    private String imageUrl; // ✅ 추가
}