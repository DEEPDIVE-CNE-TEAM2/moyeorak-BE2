package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 참조된 시설
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    // ✅ 해당 대관이 속한 지역
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    // ✅ 대관 신청 분류 (ex. 체육, 예술 등)
    @Column(length = 50, nullable = false)
    private String category;

    // ✅ 대관 대상 (ex. 아동, 성인 등)
    @Column(length = 50, nullable = false)
    private String target;

    // ✅ 대관 가능 기간
    @Column(nullable = false)
    private LocalDate usageStartDate;

    @Column(nullable = false)
    private LocalDate usageEndDate;

    // ✅ 대관 가능 시간
    @Column(nullable = false)
    private LocalTime usageStartTime;

    @Column(nullable = false)
    private LocalTime usageEndTime;

    // ✅ 대관 신청 가능한 기간
    @Column(nullable = false)
    private LocalDate registrationStartDate;

    @Column(nullable = false)
    private LocalDate registrationEndDate;

    // ✅ 대관 취소 가능 마감일
    @Column(nullable = false)
    private LocalDate cancelEndDate;

    // ✅ 신청 시 최대 인원
    @Column(nullable = false)
    private Integer capacity;
}