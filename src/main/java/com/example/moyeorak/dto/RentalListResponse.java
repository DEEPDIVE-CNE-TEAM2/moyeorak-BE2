package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalListResponse {

    private Integer id;

    // 시설 정보
    private String location;
    private String address;
    private String imageUrl;
    private String usageTime;

    private Integer capacity;
}
