package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRange {
    private LocalTime startTime;
    private LocalTime endTime;
}
