package com.vicky.ridetrack.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String status = "OFFLINE"; // ONLINE, OFFLINE, ON_TRIP

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}