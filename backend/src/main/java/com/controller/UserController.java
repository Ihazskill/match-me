package com.controller;

import com.dto.UserBasicDTO;
import com.dto.UserBioDTO;
import com.dto.UserProfileDTO;
import com.model.Bio;
import com.model.Profile;
import com.model.User;
import com.service.ProfileService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ProfileService profileService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBasicInfo(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElse(null);
        if (user == null || !user.isProfileCompleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Returns the authenticated user's basic profile info (for My Profile page).
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(Principal principal) {
        User user = userService.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileService.getProfile(user.getId()).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        UserBasicDTO dto = new UserBasicDTO(
                user.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getProfilePictureUrl(),
                profile.getAge()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns the authenticated user's full profile (for Edit Profile page pre-fill).
     */
    @GetMapping("/me/profile")
    public ResponseEntity<?> getMeProfile(Principal principal) {
        User user = userService.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileService.getProfile(user.getId()).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
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
     * Returns the authenticated user's bio (for Edit Profile bio section).
     */
    @GetMapping("/me/bio")
    public ResponseEntity<?> getMeBio(Principal principal) {
        User user = userService.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Bio bio = profileService.getBio(user.getId()).orElse(null);
        if (bio == null) {
            return ResponseEntity.notFound().build();
        }
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