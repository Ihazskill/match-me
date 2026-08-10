package com.controller;

import com.dto.ConnectionDTO;
import com.dto.ConnectionRequestDTO;
import com.model.Connection;
import com.model.Profile;
import com.model.User;
import com.service.ConnectionService;
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
public class ConnectionController {

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendationService recommendationService;

    /**
     * GET /connections - Get all connections (only IDs)
     */
    @GetMapping("/connections")
    public ResponseEntity<?> getConnections(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> connectionIds = connectionService.getConnectedUserIds(user.getId());

        List<ConnectionDTO> dtos = connectionIds.stream()
                .map(ConnectionDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /connections/pending - Get pending connection requests
     */
    @GetMapping("/connections/pending")
    public ResponseEntity<?> getPendingRequests(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ConnectionRequestDTO> pending = connectionService.getPendingRequests(user.getId())
            .stream()
            .map(connection -> {
                Profile profile = connection.getRequester().getProfile();
                String name = profile == null
                    ? "Unknown user"
                    : profile.getFirstName() + " " + profile.getLastName();
                return new ConnectionRequestDTO(
                    connection.getId(),
                    connection.getRequester().getId(),
                    name,
                    profile == null ? null : profile.getProfilePictureUrl(),
                    connection.getRequestedAt()
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(pending);
    }

    /**
     * POST /users/{id}/connect - Send connection request
     */
    @PostMapping("/users/{id}/connect")
    public ResponseEntity<?> sendConnectionRequest(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!recommendationService.isRecommendedFor(currentUser.getId(), id)) {
                return ResponseEntity.notFound().build();
            }

            Connection connection = connectionService.sendConnectionRequest(currentUser.getId(), id);

            return ResponseEntity.ok(new ConnectionDTO(connection.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /connections/{id}/accept - Accept connection request
     */
    @PostMapping("/connections/{id}/accept")
    public ResponseEntity<?> acceptConnection(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Connection connection = connectionService.acceptConnectionRequest(id, currentUser.getId());

            return ResponseEntity.ok(new ConnectionDTO(connection.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /connections/{id}/reject - Reject connection request
     */
    @PostMapping("/connections/{id}/reject")
    public ResponseEntity<?> rejectConnection(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Connection connection = connectionService.rejectConnectionRequest(id, currentUser.getId());

            return ResponseEntity.ok(new ConnectionDTO(connection.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /connections/{id}/disconnect - Disconnect from user
     */
    @PostMapping("/connections/{id}/disconnect")
    public ResponseEntity<?> disconnect(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            connectionService.disconnectFromUser(currentUser.getId(), id);

            return ResponseEntity.ok("Disconnected successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}