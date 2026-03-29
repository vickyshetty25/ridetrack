package com.vicky.ridetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NearbyDriverResponse {
    private Long driverId;
    private String driverName;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String surgeMultiplier;
}