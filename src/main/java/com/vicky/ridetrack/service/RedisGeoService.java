package com.vicky.ridetrack.service;

import com.vicky.ridetrack.dto.NearbyDriverResponse;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisGeoService {

    private final RedisTemplate<String, String> redisTemplate;

    // All driver locations stored under this key
    private static final String DRIVER_GEO_KEY = "drivers:locations";

    // Driver location expires in 10 seconds
    // If driver disconnects, they auto-disappear from search!
    private static final long DRIVER_TTL_SECONDS = 10;

    public RedisGeoService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Called every time driver sends location update
    public void updateDriverLocation(Long driverId,
                                     Double latitude,
                                     Double longitude) {
        // GEOADD stores lat/lng as 52-bit geohash in sorted set
        redisTemplate.opsForGeo().add(
                DRIVER_GEO_KEY,
                new Point(longitude, latitude), // Note: Redis uses lng,lat order
                driverId.toString()
        );

        // Store individual driver key with TTL
        // This is how we auto-remove disconnected drivers!
        String driverKey = "driver:location:" + driverId;
        redisTemplate.opsForValue().set(
                driverKey,
                latitude + "," + longitude,
                DRIVER_TTL_SECONDS,
                TimeUnit.SECONDS
        );

        System.out.println("Driver " + driverId +
                " location updated: " + latitude + ", " + longitude);
    }

    // Remove driver from GEO index when they go offline
    public void removeDriver(Long driverId) {
        redisTemplate.opsForGeo().remove(
                DRIVER_GEO_KEY,
                driverId.toString()
        );
        redisTemplate.delete("driver:location:" + driverId);
        System.out.println("Driver " + driverId + " removed from availability pool");
    }

    // GEOSEARCH finds all drivers within radius
    // O(log n + results) — incredibly fast even with 10,000 drivers!
    public List<String> findNearbyDrivers(Double latitude,
                                          Double longitude,
                                          double radiusKm) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVER_GEO_KEY,
                        new Circle(
                                new Point(longitude, latitude),
                                new Distance(radiusKm,
                                        Metrics.KILOMETERS)
                        ),
                        RedisGeoCommands.GeoRadiusCommandArgs
                                .newGeoRadiusArgs()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<String> driverIds = new ArrayList<>();
        if (results != null) {
            results.getContent().forEach(result -> {
                driverIds.add(result.getContent().getName());
            });
        }
        return driverIds;
    }

    public Distance getDistance(Long driverId,
                                Double latitude,
                                Double longitude) {
        try {
            return redisTemplate.opsForGeo().distance(
                    DRIVER_GEO_KEY,
                    driverId.toString(),
                    "rider:location",
                    Metrics.KILOMETERS
            );
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isDriverAvailable(Long driverId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("driver:location:" + driverId)
        );
    }
}