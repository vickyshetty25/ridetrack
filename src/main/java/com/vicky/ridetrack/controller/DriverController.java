package com.vicky.ridetrack.controller;

import com.vicky.ridetrack.dto.NearbyDriverResponse;
import com.vicky.ridetrack.model.Driver;
import com.vicky.ridetrack.repository.DriverRepository;
import com.vicky.ridetrack.service.RedisGeoService;
import com.vicky.ridetrack.service.SurgePricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverRepository driverRepository;
    private final RedisGeoService redisGeoService;
    private final SurgePricingService surgePricingService;

    public DriverController(DriverRepository driverRepository,
                            RedisGeoService redisGeoService,
                            SurgePricingService surgePricingService) {
        this.driverRepository = driverRepository;
        this.redisGeoService = redisGeoService;
        this.surgePricingService = surgePricingService;
    }

    @PostMapping("/register")
    public ResponseEntity<Driver> register(@RequestBody Driver driver) {
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @PostMapping("/{driverId}/location")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long driverId,
            @RequestBody Map<String, Double> location) {
        Double lat = location.get("latitude");
        Double lng = location.get("longitude");
        redisGeoService.updateDriverLocation(driverId, lat, lng);
        return ResponseEntity.ok(Map.of(
                "message", "Location updated",
                "driverId", driverId,
                "latitude", lat,
                "longitude", lng
        ));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> getNearbyDrivers(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5.0") Double radius) {

        List<String> driverIds = redisGeoService.findNearbyDrivers(lat, lng, radius);
        List<NearbyDriverResponse> responses = new ArrayList<>();

        for (String driverId : driverIds) {
            driverRepository.findById(Long.valueOf(driverId)).ifPresent(driver ->
                    responses.add(new NearbyDriverResponse(
                            driver.getId(),
                            driver.getName(),
                            lat, lng,
                            0.0,
                            surgePricingService.getCurrentSurge().toString() + "x"
                    ))
            );
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/surge")
    public ResponseEntity<?> getSurge() {
        return ResponseEntity.ok(Map.of(
                "multiplier", surgePricingService.getCurrentSurge(),
                "message", surgePricingService.getCurrentSurge()
                        .compareTo(java.math.BigDecimal.ONE) > 0
                        ? "Surge pricing active!" : "Normal pricing"
        ));
    }
}