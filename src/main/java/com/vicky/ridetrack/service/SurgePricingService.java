package com.vicky.ridetrack.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@Service
public class SurgePricingService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SURGE_KEY = "surge:multiplier";

    public SurgePricingService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Called by @Scheduled every 30 seconds
    public void calculateSurge(int activeRiders, int availableDrivers) {
        BigDecimal multiplier;

        if (availableDrivers == 0) {
            multiplier = new BigDecimal("2.0");
        } else {
            double ratio = (double) activeRiders / availableDrivers;
            if (ratio > 2.0) {
                multiplier = new BigDecimal("2.0");
            } else if (ratio > 1.5) {
                multiplier = new BigDecimal("1.5");
            } else if (ratio > 1.0) {
                multiplier = new BigDecimal("1.2");
            } else {
                multiplier = new BigDecimal("1.0");
            }
        }

        // Store in Redis — instant access for fare calculation
        redisTemplate.opsForValue().set(
                SURGE_KEY,
                multiplier.toString(),
                60, TimeUnit.SECONDS
        );

        System.out.println("Surge multiplier updated: " + multiplier + "x " +
                "(riders: " + activeRiders +
                ", drivers: " + availableDrivers + ")");
    }

    public BigDecimal getCurrentSurge() {
        String surge = redisTemplate.opsForValue().get(SURGE_KEY);
        return surge != null
                ? new BigDecimal(surge)
                : BigDecimal.ONE;
    }
}