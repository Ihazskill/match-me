package com.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class LocationDataInitializer implements CommandLineRunner {

    private static final List<LocationSeed> DEFAULT_LOCATIONS = List.of(
            new LocationSeed("Amsterdam", "Netherlands", "North Holland"),
            new LocationSeed("Berlin", "Germany", "Berlin"),
            new LocationSeed("Chicago", "USA", "Illinois"),
            new LocationSeed("London", "United Kingdom", "England"),
            new LocationSeed("Los Angeles", "USA", "California"),
            new LocationSeed("Madrid", "Spain", "Community of Madrid"),
            new LocationSeed("Melbourne", "Australia", "Victoria"),
            new LocationSeed("Mexico City", "Mexico", "Mexico City"),
            new LocationSeed("New York", "USA", "New York"),
            new LocationSeed("Paris", "France", "Ile-de-France"),
            new LocationSeed("San Francisco", "USA", "California"),
            new LocationSeed("Seattle", "USA", "Washington"),
            new LocationSeed("Singapore", "Singapore", "Central Region"),
            new LocationSeed("Sydney", "Australia", "New South Wales"),
            new LocationSeed("Tokyo", "Japan", "Tokyo")
    );

    private final LocationService locationService;

    public LocationDataInitializer(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void run(String... args) {
        DEFAULT_LOCATIONS.forEach(location -> locationService.findOrCreateLocation(
                location.city(),
                location.country(),
                location.region()
        ));
    }

    private record LocationSeed(String city, String country, String region) {
    }
}