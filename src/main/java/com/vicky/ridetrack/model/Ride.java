package com.vicky.ridetrack.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Data
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rider_id", nullable = false)
    private Long riderId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Column(name = "pickup_lng")
    private Double pickupLng;

    @Column(name = "dropoff_lat")
    private Double dropoffLat;

    @Column(name = "dropoff_lng")
    private Double dropoffLng;

    @Column(nullable = false)
    private String status = "REQUESTED"; // REQUESTED, ACCEPTED, IN_PROGRESS, COMPLETED

    @Column(name = "fare_amount")
    private BigDecimal fareAmount;

    @Column(name = "surge_multiplier")
    private BigDecimal surgeMultiplier = BigDecimal.ONE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}