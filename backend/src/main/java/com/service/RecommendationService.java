package com.service;

import com.model.*;
import com.repository.ConnectionRepository;
import com.repository.DismissedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecommendationService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private DismissedUserRepository dismissedUserRepository;

    /**
     * Get top 10 recommendations for a user
     */
    public List<User> getRecommendations(Long userId) {
        User currentUser = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if current user has completed profile
        if (!currentUser.isProfileCompleted()) {
            throw new RuntimeException("Please complete your profile before viewing recommendations");
        }

        Bio currentUserBio = profileService.getBio(userId)
                .orElseThrow(() -> new RuntimeException("Bio not found"));

        // Get all users with completed profiles (excluding current user)
        List<User> allUsers = userService.getAllUsersWithCompletedProfiles(userId);

        // Filter out users who shouldn't be recommended
        List<User> eligibleUsers = filterIneligibleUsers(currentUser, allUsers);

        // Score each eligible user
        Map<User, Double> scoredUsers = new HashMap<>();
        for (User candidate : eligibleUsers) {
            double score = calculateCompatibilityScore(currentUserBio, candidate);
            scoredUsers.put(candidate, score);
        }

        // Sort by score (highest first) and return top 10
        return scoredUsers.entrySet().stream()
                .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Filter out users who shouldn't be recommended:
     * - Users already dismissed
     * - Users with existing connections (pending or accepted)
     * - Users in different locations (if location matching is required)
     */
    private List<User> filterIneligibleUsers(User currentUser, List<User> allUsers) {
        // Get dismissed users
        Set<Long> dismissedUserIds = dismissedUserRepository.findByUser(currentUser)
                .stream()
                .map(d -> d.getDismissedUser().getId())
                .collect(Collectors.toSet());

        // Get users with existing connections
        Set<Long> connectedUserIds = connectionRepository.findByUserAndStatus(
                        currentUser, Connection.ConnectionStatus.ACCEPTED
                ).stream()
                .map(c -> c.getRequester().equals(currentUser) ?
                        c.getReceiver().getId() : c.getRequester().getId())
                .collect(Collectors.toSet());

        // Get users with pending connection requests (both directions)
        Set<Long> pendingUserIds = connectionRepository.findByUserAndStatus(
                        currentUser, Connection.ConnectionStatus.PENDING
                ).stream()
                .map(c -> c.getRequester().equals(currentUser) ?
                        c.getReceiver().getId() : c.getRequester().getId())
                .collect(Collectors.toSet());

        // Filter out ineligible users
        return allUsers.stream()
                .filter(user -> !dismissedUserIds.contains(user.getId()))
                .filter(user -> !connectedUserIds.contains(user.getId()))
                .filter(user -> !pendingUserIds.contains(user.getId()))
                .filter(user -> isLocationCompatible(currentUser, user))
                .collect(Collectors.toList());
    }

    /**
     * Check if two users are in compatible locations
     */
    private boolean isLocationCompatible(User user1, User user2) {
        Bio bio1 = user1.getBio();
        Bio bio2 = user2.getBio();

        if (bio1 == null || bio2 == null) {
            return false;
        }

        Location loc1 = bio1.getLocation();
        Location loc2 = bio2.getLocation();

        // If either user doesn't have a location, skip location check
        if (loc1 == null || loc2 == null) {
            return true;
        }

        // Users must be in the same city
        return loc1.getCity().equalsIgnoreCase(loc2.getCity()) &&
                loc1.getCountry().equalsIgnoreCase(loc2.getCountry());
    }

    /**
     * Calculate compatibility score between current user and candidate
     * Based on at least 5 biographical data points
     *
     * Score breakdown:
     * - Interests match: 20% (configurable by user)
     * - Hobbies match: 20%
     * - Music taste: 15%
     * - Food preference: 15%
     * - Lifestyle: 15%
     * - Personality: 15%
     * - Mutual "looking for" match: BONUS
     */
    private double calculateCompatibilityScore(Bio currentUserBio, User candidate) {
        Bio candidateBio = candidate.getBio();

        if (candidateBio == null) {
            return 0.0;
        }

        double totalScore = 0.0;

        // 1. Interests match (using user's weight preference)
        double interestScore = calculateSetSimilarity(
                currentUserBio.getInterests(),
                candidateBio.getInterests()
        ) * currentUserBio.getInterestWeight();
        totalScore += interestScore;

        // 2. Hobbies match
        double hobbyScore = calculateSetSimilarity(
                currentUserBio.getHobbies(),
                candidateBio.getHobbies()
        ) * currentUserBio.getHobbyWeight();
        totalScore += hobbyScore;

        // 3. Music taste match
        double musicScore = calculateStringSimilarity(
                currentUserBio.getMusicTaste(),
                candidateBio.getMusicTaste()
        ) * currentUserBio.getMusicWeight();
        totalScore += musicScore;

        // 4. Food preference match
        double foodScore = calculateStringSimilarity(
                currentUserBio.getFoodPreference(),
                candidateBio.getFoodPreference()
        ) * currentUserBio.getFoodWeight();
        totalScore += foodScore;

        // 5. Lifestyle match
        double lifestyleScore = calculateStringSimilarity(
                currentUserBio.getLifestyle(),
                candidateBio.getLifestyle()
        ) * currentUserBio.getLifestyleWeight();
        totalScore += lifestyleScore;

        // 6. Personality match
        double personalityScore = calculateStringSimilarity(
                currentUserBio.getPersonality(),
                candidateBio.getPersonality()
        ) * currentUserBio.getPersonalityWeight();
        totalScore += personalityScore;

        // 7. BONUS: Mutual "looking for" match
        // If current user's interests match what candidate is seeking, AND vice versa
        double mutualLookingForBonus = calculateMutualLookingForScore(currentUserBio, candidateBio);
        totalScore += mutualLookingForBonus;

        // 8. BONUS: Location preference match
        if (isLocationPreferenceMatch(currentUserBio, candidateBio)) {
            totalScore += 10.0; // Bonus points for matching location preferences
        }

        return totalScore;
    }

    /**
     * Calculate similarity between two sets (e.g., interests, hobbies)
     * Returns a value between 0.0 and 1.0
     */
    private double calculateSetSimilarity(Set<String> set1, Set<String> set2) {
        if (set1 == null || set2 == null || set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        // Jaccard similarity: intersection / union
        return (double) intersection.size() / union.size();
    }

    /**
     * Calculate similarity between two strings
     * Returns 1.0 if equal (case-insensitive), 0.0 if different or null
     */
    private double calculateStringSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null || str1.isEmpty() || str2.isEmpty()) {
            return 0.0;
        }

        return str1.equalsIgnoreCase(str2) ? 1.0 : 0.0;
    }

    /**
     * Calculate mutual "looking for" score
     * If user A's interests match what user B is seeking, and vice versa
     */
    private double calculateMutualLookingForScore(Bio bio1, Bio bio2) {
        double score = 0.0;

        // Check if bio1's interests match what bio2 is seeking
        if (bio1.getInterests() != null && bio2.getSeekingInterests() != null) {
            double forwardMatch = calculateSetSimilarity(
                    bio1.getInterests(),
                    bio2.getSeekingInterests()
            );
            score += forwardMatch * 15; // Bonus weight
        }

        // Check if bio2's interests match what bio1 is seeking
        if (bio2.getInterests() != null && bio1.getSeekingInterests() != null) {
            double reverseMatch = calculateSetSimilarity(
                    bio2.getInterests(),
                    bio1.getSeekingInterests()
            );
            score += reverseMatch * 15; // Bonus weight
        }

        return score;
    }

    /**
     * Check if location preferences match
     * User1 wants someone from location X, and User2 is from location X
     */
    private boolean isLocationPreferenceMatch(Bio bio1, Bio bio2) {
        if (bio1.getSeekingLocation() != null && bio2.getLocation() != null) {
            return bio1.getSeekingLocation().getId().equals(bio2.getLocation().getId());
        }
        return false;
    }

    /**
     * Dismiss a user (they won't appear in recommendations again)
     */
    public void dismissUser(Long currentUserId, Long userToDismissId) {
        User currentUser = userService.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        User userToDismiss = userService.findById(userToDismissId)
                .orElseThrow(() -> new RuntimeException("User to dismiss not found"));

        // Check if already dismissed
        if (dismissedUserRepository.existsByUsers(currentUser, userToDismiss)) {
            return; // Already dismissed
        }

        // Create dismissal record
        DismissedUser dismissal = new DismissedUser();
        dismissal.setDismissedBy(currentUser);
        dismissal.setDismissedUser(userToDismiss);
        dismissedUserRepository.save(dismissal);
    }
}