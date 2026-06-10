package com.controller;

import com.model.DismissedUser;
import com.model.User;
import com.repository.ConnectionRepository;
import com.repository.DismissedUserRepository;
import com.service.ConnectionService;
import com.service.ProfileService;
import com.service.RecommendationService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
   private UserService userService;
    @Autowired
    private ProfileService profileService;

    @GetMapping("/user")
    public ResponseEntity<?> getUserBasicInfo(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElse(null);
        if (user == null || !user.isProfileCompleted()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }


}
