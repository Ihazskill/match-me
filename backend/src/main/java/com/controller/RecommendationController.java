package com.controller;

import com.dto.RecommendationDTO;
import com.model.User;
import com.service.RecommendationService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    /**
     * GET /recommendations - Get top 10 recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<User> recommendations = recommendationService.getRecommendations(user.getId());

            // Convert to DTOs (only IDs as per requirements)
            List<RecommendationDTO> dtos = recommendations.stream()
                    .map(u -> new RecommendationDTO(u.getId()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /users/{id}/dismiss - Dismiss a user from recommendations
     */
    @PostMapping("/users/{id}/dismiss")
    public ResponseEntity<?> dismissUser(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        recommendationService.dismissUser(currentUser.getId(), id);

        return ResponseEntity.ok("User dismissed");
    }
}