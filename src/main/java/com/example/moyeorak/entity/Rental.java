package com.example.moyeorak.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(length = 255, nullable = false)
    private String location;

    @Column(length = 255)
    private String imageUrl;

    @Lob
    private String description;

    @Column(length = 50, nullable = false)
    private String target;

    @Column(nullable = false)
    private LocalDate usageStartDate;

    @Column(nullable = false)
    private LocalDate usageEndDate;

    @Column(nullable = false)
    private LocalTime usageStartTime;

    @Column(nullable = false)
    private LocalTime usageEndTime;

    @Column(nullable = false)
    private LocalDate registrationStartDate;

    @Column(nullable = false)
    private LocalDate registrationEndDate;

    @Column(nullable = false)
    private LocalDate cancelEndDate;

    @Column(nullable = false)
    private Integer fee;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 50, nullable = false)
    private String contact;
}

