package com.vicky.ridetrack.dto;

import lombok.Data;

// Driver sends this via WebSocket every 3 seconds
@Data
public class LocationUpdate {
    private Long driverId;
    private Double latitude;
    private Double longitude;
    private String status; // ONLINE, OFFLINE
}