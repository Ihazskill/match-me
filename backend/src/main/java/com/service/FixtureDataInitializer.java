package com.service;

import com.model.Location;
import com.model.User;
import com.repository.LocationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Profile("fixtures")
@Order(2)
public class FixtureDataInitializer implements CommandLineRunner {

    private static final String[] FIRST_NAMES = {
            "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Jamie", "Avery", "Cameron", "Drew",
            "Emerson", "Finley", "Harper", "Hayden", "Jules", "Kai", "Logan", "Parker", "Quinn", "Reese"
    };
    private static final String[] LAST_NAMES = {
            "Anderson", "Bennett", "Chen", "Diaz", "Evans", "Foster", "Garcia", "Hughes", "Ivanov", "Jones"
    };
    private static final String[][] INTERESTS = {
            {"live music", "travel", "coffee"},
            {"technology", "board games", "cooking"},
            {"fitness", "hiking", "photography"},
            {"books", "films", "museums"},
            {"dogs", "parks", "community"}
    };
    private static final String[][] HOBBIES = {
            {"concerts", "guitar"}, {"coding", "chess"}, {"running", "camping"},
            {"reading", "writing"}, {"dog walking", "gardening"}
    };
    private static final String[] MUSIC = {"pop", "rock", "jazz", "classical", "electronic"};
    private static final String[] FOOD = {"vegan", "vegetarian", "meat", "seafood", "anything"};
    private static final String[] TRAVEL = {"adventurer", "relaxed", "cultural", "homebody"};
    private static final String[] LIFESTYLE = {"active", "balanced", "relaxed", "workaholic"};
    private static final String[] PERSONALITY = {"introvert", "extrovert", "ambivert"};

    private final UserService userService;
    private final ProfileService profileService;
    private final LocationRepository locationRepository;

    public FixtureDataInitializer(
            UserService userService,
            ProfileService profileService,
            LocationRepository locationRepository) {
        this.userService = userService;
        this.profileService = profileService;
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(String... args) {
        List<Location> locations = locationRepository.findAll();
        if (locations.isEmpty()) {
            throw new IllegalStateException("Locations must be initialized before fixtures");
        }

        for (int index = 0; index < 100; index++) {
            String email = "reviewer" + (index + 1) + "@matchme.test";
            if (userService.findByEmail(email).isPresent()) {
                continue;
            }

            User user = userService.createUser(email, "Review123!");
            int group = index % INTERESTS.length;
            Location location = locations.get(index % locations.size());
            profileService.saveProfile(
                    user.getId(),
                    FIRST_NAMES[index % FIRST_NAMES.length],
                    LAST_NAMES[(index / FIRST_NAMES.length) % LAST_NAMES.length],
                    "Looking for thoughtful people to share activities, ideas, and good conversation with.",
                    21 + (index % 40),
                    null
            );
            profileService.saveBio(
                    user.getId(),
                    Set.of(INTERESTS[group]),
                    Set.of(HOBBIES[group]),
                    MUSIC[group],
                    FOOD[group],
                    TRAVEL[index % TRAVEL.length],
                    LIFESTYLE[index % LIFESTYLE.length],
                    PERSONALITY[index % PERSONALITY.length],
                    location.getId(),
                    null,
                    null,
                    null,
                    "A compatible local connection",
                    Set.of(INTERESTS[group]),
                    location.getId()
            );
        }
    }
}