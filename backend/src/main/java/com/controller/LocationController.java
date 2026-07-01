package com.controller;

import com.model.Location;
import com.service.LocationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * GET /api/locations - Get all locations
     */
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * GET /api/locations/{id} - Get location by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/locations - Create a new location
     * Body: { "city": "New York", "country": "USA", "region": "Northeast" }
     */
    @PostMapping
    public ResponseEntity<?> createLocation(@RequestBody LocationRequest request) {
        try {
            Location location = locationService.createLocation(
                    request.getCity(),
                    request.getCountry(),
                    request.getRegion()
            );
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DTO for request body
    @Getter
    @Setter
    public static class LocationRequest {
        private String city;
        private String country;
        private String region;
    }
}