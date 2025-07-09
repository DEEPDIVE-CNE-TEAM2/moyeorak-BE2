package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Integer> {
    List<Rental> findByRegionId(Long regionId);
}
