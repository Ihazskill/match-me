package com.controller;

import com.dto.BioUpdateRequest;
import com.dto.ProfileUpdateRequest;
import com.model.Bio;
import com.model.Profile;
import com.model.User;
import com.service.ProfileService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    /**
     * POST /profile - Create or update profile
     */
    @PostMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileService.saveProfile(
                user.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getAboutMe(),
                request.getAge(),
                request.getProfilePictureUrl()
        );

        return ResponseEntity.ok(profile);
    }

    /**
     * POST /profile/bio - Create or update bio
     */
    @PostMapping("/bio")
    public ResponseEntity<?> updateBio(
            @RequestBody BioUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bio bio = profileService.saveBio(
                user.getId(),
                request.getInterests(),
                request.getHobbies(),
                request.getMusicTaste(),
                request.getFoodPreference(),
                request.getTravelStyle(),
                request.getLifestyle(),
                request.getPersonality(),
                request.getLocationId(),
                request.getLookingFor(),
                request.getSeekingInterests(),
                request.getSeekingLocationId()
        );

        return ResponseEntity.ok(bio);
    }
}