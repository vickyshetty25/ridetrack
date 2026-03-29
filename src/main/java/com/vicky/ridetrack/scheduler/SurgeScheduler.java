package com.vicky.ridetrack.scheduler;

import com.vicky.ridetrack.repository.DriverRepository;
import com.vicky.ridetrack.repository.RideRepository;
import com.vicky.ridetrack.service.SurgePricingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SurgeScheduler {

    private final SurgePricingService surgePricingService;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;

    public SurgeScheduler(SurgePricingService surgePricingService,
                          DriverRepository driverRepository,
                          RideRepository rideRepository) {
        this.surgePricingService = surgePricingService;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
    }

    // Runs every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void updateSurgePricing() {
        int availableDrivers = driverRepository.findByStatus("ONLINE").size();
        int activeRiders = rideRepository.findAll().stream()
                .filter(r -> r.getStatus().equals("REQUESTED") ||
                        r.getStatus().equals("IN_PROGRESS"))
                .toList().size();

        surgePricingService.calculateSurge(activeRiders, availableDrivers);
    }
}