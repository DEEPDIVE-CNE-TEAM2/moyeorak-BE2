package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalApplicationRequest {

    @NotNull
    private Integer userId;  // 신청자

    @NotNull
    private Integer rentalId; // 신청 공간

    @NotNull
    private Integer regionId; // 지역 ID

    @NotNull
    private LocalDate requestedDate; // 신청 일자

    @NotNull
    private LocalTime requestedStartTime; // 신청 시작 시간

    @NotNull
    private LocalTime requestedEndTime; // 신청 종료 시간

    @NotNull
    @Size(min = 1, max = 500)
    private String note; // 목적 및 비고
}
