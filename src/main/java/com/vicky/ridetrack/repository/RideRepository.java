package com.vicky.ridetrack.repository;

import com.vicky.ridetrack.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByRiderIdOrderByCreatedAtDesc(Long riderId);
    List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);
}