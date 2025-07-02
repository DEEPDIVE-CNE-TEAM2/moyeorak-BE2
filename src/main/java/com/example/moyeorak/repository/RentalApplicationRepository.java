package com.example.moyeorak.repository;

import com.example.moyeorak.entity.RentalApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalApplicationRepository extends JpaRepository<RentalApplication, Long> {
    List<RentalApplication> findByUserId(Long userId);
    List<RentalApplication> findByRentalId(Long rentalId);
}