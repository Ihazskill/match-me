package com.service;

import com.model.Location;
import com.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    /**
     * Get all locations
     */
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    /**
     * Find location by ID
     */
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    /**
     * Find or create location
     */
    public Location findOrCreateLocation(String city, String country, String region) {
        return locationRepository.findByCityAndCountry(city, country)
                .orElseGet(() -> {
                    Location location = new Location();
                    location.setCity(city);
                    location.setCountry(country);
                    location.setRegion(region);
                    return locationRepository.save(location);
                });
    }

    /**
     * Create a new location
     */
    public Location createLocation(String city, String country, String region) {
        Location location = new Location();
        location.setCity(city);
        location.setCountry(country);
        location.setRegion(region);
        return locationRepository.save(location);
    }
}