package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByRegionId(Long regionId);
    List<Rental> findByFacilityId(Long facilityId);
    List<Rental> findByFacilityIdAndUsageEndDateAfter(Long facilityId, LocalDate today);
    List<Rental> findByRegistrationStartDateLessThanEqualAndRegistrationEndDateGreaterThanEqual(
            LocalDate start, LocalDate end);
}
