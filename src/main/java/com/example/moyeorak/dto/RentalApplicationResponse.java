package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalApplicationResponse {

    private Long id;
    private Integer userId;
    private Integer rentalId;
    private Integer regionId;
    private LocalDate requestedDate;
    private LocalTime requestedStartTime;
    private LocalTime requestedEndTime;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    public RentalApplicationResponse(Long id, String status, String note, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
    }
}

