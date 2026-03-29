package com.vicky.ridetrack.dto;

import lombok.Data;

@Data
public class RideRequest {
    private Long riderId;
    private Double pickupLat;
    private Double pickupLng;
    private Double dropoffLat;
    private Double dropoffLng;
}