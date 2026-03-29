package com.vicky.ridetrack.repository;

import com.vicky.ridetrack.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    List<Driver> findByStatus(String status);
}