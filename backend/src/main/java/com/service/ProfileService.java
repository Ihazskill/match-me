package com.service;

import com.model.Bio;
import com.model.Location;
import com.model.Profile;
import com.model.User;
import com.repository.BioRepository;
import com.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private BioRepository bioRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private LocationService locationService;

    /**
     * Create or update user profile
     */
    public Profile saveProfile(Long userId, String firstName, String lastName,
                               String aboutMe, Integer age, String profilePictureUrl) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(userId)
                .orElse(new Profile());

        profile.setUser(user);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setAboutMe(aboutMe);
        profile.setAge(age);
        profile.setProfilePictureUrl(profilePictureUrl);

        Profile savedProfile = profileRepository.save(profile);

        // Check if profile is complete
        checkAndMarkProfileComplete(userId);

        return savedProfile;
    }

    /**
     * Get user profile
     */
    public Optional<Profile> getProfile(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    /**
     * Create or update user bio
     */
    public Bio saveBio(Long userId, Set<String> interests, Set<String> hobbies,
                       String musicTaste, String foodPreference, String travelStyle,
                       String lifestyle, String personality, Long locationId,
                       String lookingFor, Set<String> seekingInterests,
                       Long seekingLocationId) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bio bio = bioRepository.findByUserId(userId)
                .orElse(new Bio());

        bio.setUser(user);
        bio.setInterests(interests);
        bio.setHobbies(hobbies);
        bio.setMusicTaste(musicTaste);
        bio.setFoodPreference(foodPreference);
        bio.setTravelStyle(travelStyle);
        bio.setLifestyle(lifestyle);
        bio.setPersonality(personality);
        bio.setLookingFor(lookingFor);
        bio.setSeekingInterests(seekingInterests);

        // Set location if provided
        if (locationId != null) {
            Location location = locationService.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            bio.setLocation(location);
        }

        // Set seeking location if provided
        if (seekingLocationId != null) {
            Location seekingLocation = locationService.findById(seekingLocationId)
                    .orElseThrow(() -> new RuntimeException("Seeking location not found"));
            bio.setSeekingLocation(seekingLocation);
        }

        Bio savedBio = bioRepository.save(bio);

        // Check if profile is complete
        checkAndMarkProfileComplete(userId);

        return savedBio;
    }

    /**
     * Get user bio
     */
    public Optional<Bio> getBio(Long userId) {
        return bioRepository.findByUserId(userId);
    }

    /**
     * Check if user has completed their profile and mark accordingly
     */
    private void checkAndMarkProfileComplete(Long userId) {
        Optional<Profile> profile = profileRepository.findByUserId(userId);
        Optional<Bio> bio = bioRepository.findByUserId(userId);

        if (profile.isPresent() && bio.isPresent()) {
            userService.markProfileAsCompleted(userId);
        }
    }

    /**
     * Check if user has completed their profile
     */
    public boolean isProfileComplete(Long userId) {
        Optional<Profile> profile = profileRepository.findByUserId(userId);
        Optional<Bio> bio = bioRepository.findByUserId(userId);
        return profile.isPresent() && bio.isPresent();
    }
}