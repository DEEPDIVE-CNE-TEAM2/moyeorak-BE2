package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilitySimpleDto {
    private Long id;
    private String location;
    private String address;
    private String usageTime;
    private Integer capacity;
    private String imageUrl;
}

