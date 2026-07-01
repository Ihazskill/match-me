
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    // ─── /me shortcuts ───────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        User me = resolveUser(authentication);
        return buildBasicDTO(me);
    }

    @GetMapping("/me/profile")
    public ResponseEntity<?> getMeProfile(Authentication authentication) {
        User me = resolveUser(authentication);
        return buildProfileDTO(me);
    }

    @GetMapping("/me/bio")
    public ResponseEntity<?> getMeBio(Authentication authentication) {
        User me = resolveUser(authentication);
        return buildBioDTO(me);
    }

    // ─── /users/{id} endpoints ───────────────────────────────────────────────

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserBasicInfo(
            @PathVariable Long id,
            Authentication authentication) {

        User viewer = resolveUser(authentication);
        User target = findVisibleUserOrNull(id, viewer);
        if (target == null) return ResponseEntity.notFound().build();
        return buildBasicDTO(target);
    }

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<?> getUserProfile(
            @PathVariable Long id,
            Authentication authentication) {

        User viewer = resolveUser(authentication);
        User target = findVisibleUserOrNull(id, viewer);
        if (target == null) return ResponseEntity.notFound().build();
        return buildProfileDTO(target);
    }

    @GetMapping("/users/{id}/bio")
    public ResponseEntity<?> getUserBio(
            @PathVariable Long id,
            Authentication authentication) {

        User viewer = resolveUser(authentication);
        User target = findVisibleUserOrNull(id, viewer);
        if (target == null) return ResponseEntity.notFound().build();
        return buildBioDTO(target);
    }

    // ─── Visibility logic ────────────────────────────────────────────────────

    /**
     * A profile is viewable only if:
     *  - viewer is looking at themselves
     *  - there is a PENDING or ACCEPTED connection between them
     *  - the target appears in the viewer's current recommendations
     *
     * Returns null → caller responds with 404.
     */
    private User findVisibleUserOrNull(Long targetId, User viewer) {
        if (viewer.getId().equals(targetId)) return viewer;

        Optional<User> targetOpt = userService.findById(targetId);
        if (targetOpt.isEmpty()) return null;

        User target = targetOpt.get();
        if (!target.isProfileCompleted()) return null;

        Optional<Connection> connection = connectionRepository
                .findConnectionBetweenUsers(viewer, target);

        if (connection.isPresent()) {
            Connection.ConnectionStatus status = connection.get().getStatus();
            if (status == Connection.ConnectionStatus.ACCEPTED ||
                    status == Connection.ConnectionStatus.PENDING) {
                return target;
            }
            return null; // REJECTED or DISCONNECTED → not visible
        }

        // Check recommendations as a fallback
        try {
            List<User> recommendations = recommendationService.getRecommendations(viewer.getId());
            boolean isRecommended = recommendations.stream()
                    .anyMatch(u -> u.getId().equals(targetId));
            if (isRecommended) return target;
        } catch (RuntimeException ignored) {
            // viewer has no bio yet → skip recommendation check
        }

        return null;
    }

    // ─── DTO builders ────────────────────────────────────────────────────────

    private ResponseEntity<?> buildBasicDTO(User user) {
        Profile profile = profileService.getProfile(user.getId()).orElse(null);
        if (profile == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new UserBasicDTO(
                user.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getProfilePictureUrl(),
                profile.getAge()
        ));
    }

    private ResponseEntity<?> buildProfileDTO(User user) {
        Profile profile = profileService.getProfile(user.getId()).orElse(null);
        if (profile == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new UserProfileDTO(
                user.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAboutMe(),
                profile.getAge()
        ));
    }

    private ResponseEntity<?> buildBioDTO(User user) {
        Bio bio = profileService.getBio(user.getId()).orElse(null);
        if (bio == null) return ResponseEntity.notFound().build();

        String locationName = bio.getLocation() != null
                ? bio.getLocation().getCity() + ", " + bio.getLocation().getCountry()
                : null;
        String seekingLocationName = bio.getSeekingLocation() != null
                ? bio.getSeekingLocation().getCity() + ", " + bio.getSeekingLocation().getCountry()
                : null;

        return ResponseEntity.ok(new UserBioDTO(
                user.getId(),
                bio.getInterests(),
                bio.getHobbies(),
                bio.getMusicTaste(),
                bio.getFoodPreference(),
                bio.getTravelStyle(),
                bio.getLifestyle(),
                bio.getPersonality(),
                locationName,
                bio.getLookingFor(),
                bio.getSeekingInterests(),
                seekingLocationName
        ));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private User resolveUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}