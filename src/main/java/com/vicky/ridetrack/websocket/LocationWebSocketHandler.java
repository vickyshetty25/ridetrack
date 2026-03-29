package com.vicky.ridetrack.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vicky.ridetrack.dto.LocationUpdate;
import com.vicky.ridetrack.service.RedisGeoService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LocationWebSocketHandler {

    private final RedisGeoService redisGeoService;
    private final SimpMessagingTemplate messagingTemplate;

    public LocationWebSocketHandler(RedisGeoService redisGeoService,
                                    SimpMessagingTemplate messagingTemplate) {
        this.redisGeoService = redisGeoService;
        this.messagingTemplate = messagingTemplate;
    }

    // Driver sends location update to /app/driver/location
    // All riders subscribed to /topic/driver/{driverId} receive it
    @MessageMapping("/driver/location")
    public void updateLocation(LocationUpdate update) {
        if ("OFFLINE".equals(update.getStatus())) {
            redisGeoService.removeDriver(update.getDriverId());
        } else {
            redisGeoService.updateDriverLocation(
                    update.getDriverId(),
                    update.getLatitude(),
                    update.getLongitude()
            );
        }

        // Broadcast to all riders watching this driver
        messagingTemplate.convertAndSend(
                "/topic/driver/" + update.getDriverId(),
                update
        );
    }
}