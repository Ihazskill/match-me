package com.service;

import com.model.User;
import com.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by email - UPDATED to return Optional
     */
    public Optional<User> findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return Optional.ofNullable(user);  // Wrap in Optional
    }

    /**
     * Get all users with completed profiles (for recommendations)
     */
    public List<User> getAllUsersWithCompletedProfiles(Long excludeUserId) {
        return userRepository.findAllWithCompletedProfiles(excludeUserId);
    }

    /**
     * Create new user (registration)
     */
    public User createUser(String email, String password) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProfileCompleted(false);
        user.setOnline(false);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Mark user profile as completed
     */
    public void markProfileAsCompleted(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfileCompleted(true);
        userRepository.save(user);
    }

    /**
     * Update user online status
     */
    public void updateOnlineStatus(Long userId, boolean online) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setOnline(online);
        if (!online) {
            user.setLastSeen(LocalDateTime.now());
        }
        userRepository.save(user);
    }
}