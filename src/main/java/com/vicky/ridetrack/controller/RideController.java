package com.vicky.ridetrack.controller;

import com.vicky.ridetrack.dto.RideRequest;
import com.vicky.ridetrack.model.Ride;
import com.vicky.ridetrack.repository.RideRepository;
import com.vicky.ridetrack.service.RedisGeoService;
import com.vicky.ridetrack.service.SurgePricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideRepository rideRepository;
    private final RedisGeoService redisGeoService;
    private final SurgePricingService surgePricingService;

    public RideController(RideRepository rideRepository,
                          RedisGeoService redisGeoService,
                          SurgePricingService surgePricingService) {
        this.rideRepository = rideRepository;
        this.redisGeoService = redisGeoService;
        this.surgePricingService = surgePricingService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestRide(@RequestBody RideRequest request) {
        // Find nearest available driver within 5km
        List<String> nearbyDrivers = redisGeoService.findNearbyDrivers(
                request.getPickupLat(),
                request.getPickupLng(),
                5.0
        );

        if (nearbyDrivers.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of("message", "No drivers available nearby"));
        }

        // Take the closest driver (first result — already sorted by distance)
        Long driverId = Long.valueOf(nearbyDrivers.get(0));

        // Create ride with surge pricing
        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setDriverId(driverId);
        ride.setPickupLat(request.getPickupLat());
        ride.setPickupLng(request.getPickupLng());
        ride.setDropoffLat(request.getDropoffLat());
        ride.setDropoffLng(request.getDropoffLng());
        ride.setStatus("ACCEPTED");
        ride.setSurgeMultiplier(surgePricingService.getCurrentSurge());

        // Base fare calculation
        BigDecimal baseFare = new BigDecimal("50.00");
        ride.setFareAmount(baseFare.multiply(
                surgePricingService.getCurrentSurge()));

        rideRepository.save(ride);

        return ResponseEntity.ok(Map.of(
                "rideId", ride.getId(),
                "driverId", driverId,
                "status", "ACCEPTED",
                "fareAmount", ride.getFareAmount(),
                "surgeMultiplier", ride.getSurgeMultiplier(),
                "message", "Driver found and ride accepted!"
        ));
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<?> completeRide(@PathVariable Long rideId) {
        return rideRepository.findById(rideId).map(ride -> {
            ride.setStatus("COMPLETED");
            rideRepository.save(ride);
            return ResponseEntity.ok(Map.of(
                    "message", "Ride completed!",
                    "fareAmount", ride.getFareAmount()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<Ride>> getRiderHistory(
            @PathVariable Long riderId) {
        return ResponseEntity.ok(
                rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId));
    }
}