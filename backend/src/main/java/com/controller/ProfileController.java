package com.controller;

import com.dto.BioUpdateRequest;
import com.dto.ProfileUpdateRequest;
import com.dto.UserProfileDTO;
import com.dto.UserBioDTO;
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

        UserProfileDTO dto = new UserProfileDTO(
                user.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAboutMe(),
                profile.getAge()
        );
        return ResponseEntity.ok(dto);
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

        UserBioDTO dto = new UserBioDTO(
                bio.getId(),
                bio.getInterests(),
                bio.getHobbies(),
                bio.getMusicTaste(),
                bio.getFoodPreference(),
                bio.getTravelStyle(),
                bio.getLifestyle(),
                bio.getPersonality(),
                bio.getLocation() != null ? bio.getLocation().getCity() : null,
                bio.getLookingFor(),
                bio.getSeekingInterests(),
                bio.getSeekingLocation() != null ? bio.getSeekingLocation().getCity() : null
        );
        return ResponseEntity.ok(dto);
    }
}