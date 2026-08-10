package com.controller;

import com.dto.UserBasicDTO;
import com.dto.UserBioDTO;
import com.dto.UserProfileDTO;
import com.model.Bio;
import com.model.Connection;
import com.model.Profile;
import com.model.User;
import com.repository.ConnectionRepository;
import com.service.ProfileService;
import com.service.RecommendationService;
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
    @Autowired
    private ConnectionRepository connectionRepository;
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBasicInfo(@PathVariable Long userId, Principal principal) {
        return getUserById(userId, principal);
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
            profile.getAge(),
            user.isOnline()
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
            user.getId(),
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
                bio.getSeekingLocation() != null ? bio.getSeekingLocation().getCity() : null,
                bio.getLatitude(),
                bio.getLongitude(),
                bio.getMaxRadiusKm()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * Checks if the viewer is allowed to see the target user's profile.
     * Per spec: viewable only if recommended, pending request, or connected.
     */
    private boolean canView(User viewer, User target) {
        if (viewer.getId().equals(target.getId())) {
            return true;
        }
        return connectionRepository.findConnectionBetweenUsers(viewer, target)
                .map(c -> c.getStatus() == Connection.ConnectionStatus.PENDING
                        || c.getStatus() == Connection.ConnectionStatus.ACCEPTED)
            .orElseGet(() -> recommendationService.isRecommendedFor(viewer.getId(), target.getId()));
    }

    /**
     * Returns another user's basic info (name, picture) — if viewer has permission.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Principal principal) {
        User viewer = userService.findByEmail(principal.getName()).orElse(null);
        User target = userService.findById(id).orElse(null);
        if (viewer == null || target == null || !canView(viewer, target)) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileService.getProfile(target.getId()).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        UserBasicDTO dto = new UserBasicDTO(
            target.getId(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getProfilePictureUrl(),
            profile.getAge(),
            target.isOnline()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns another user's "about me" info — if viewer has permission.
     */
    @GetMapping("/users/{id}/profile")
    public ResponseEntity<?> getUserProfileById(@PathVariable Long id, Principal principal) {
        User viewer = userService.findByEmail(principal.getName()).orElse(null);
        User target = userService.findById(id).orElse(null);
        if (viewer == null || target == null || !canView(viewer, target)) {
            return ResponseEntity.notFound().build();
        }
        Profile profile = profileService.getProfile(target.getId()).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        UserProfileDTO dto = new UserProfileDTO(
                target.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAboutMe(),
                profile.getAge()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns another user's biographical/matching data — if viewer has permission.
     */
    @GetMapping("/users/{id}/bio")
    public ResponseEntity<?> getUserBioById(@PathVariable Long id, Principal principal) {
        User viewer = userService.findByEmail(principal.getName()).orElse(null);
        User target = userService.findById(id).orElse(null);
        if (viewer == null || target == null || !canView(viewer, target)) {
            return ResponseEntity.notFound().build();
        }
        Bio bio = profileService.getBio(target.getId()).orElse(null);
        if (bio == null) {
            return ResponseEntity.notFound().build();
        }
        UserBioDTO dto = new UserBioDTO(
            target.getId(),
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
                bio.getSeekingLocation() != null ? bio.getSeekingLocation().getCity() : null,
                bio.getLatitude(),
                bio.getLongitude(),
                bio.getMaxRadiusKm()
        );
        return ResponseEntity.ok(dto);
    }
}