package com.service;

import com.model.Bio;
import com.model.Connection;
import com.model.Location;
import com.model.User;
import com.repository.ConnectionRepository;
import com.repository.DismissedUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTests {

    @Mock
    private UserService userService;
    @Mock
    private ProfileService profileService;
    @Mock
    private ConnectionRepository connectionRepository;
    @Mock
    private DismissedUserRepository dismissedUserRepository;
    @InjectMocks
    private RecommendationService recommendationService;

    private User currentUser;
    private Bio currentBio;
    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location(1L, "London", "United Kingdom", "England");
        currentUser = user(1L, true);
        currentBio = bio(currentUser, Set.of("music"), "rock");
        currentUser.setBio(currentBio);

        when(userService.findById(1L)).thenReturn(Optional.of(currentUser));
        when(profileService.getBio(1L)).thenReturn(Optional.of(currentBio));
        when(dismissedUserRepository.findByUser(currentUser)).thenReturn(Collections.emptyList());
        when(connectionRepository.findByUserAndStatus(eq(currentUser), any(Connection.ConnectionStatus.class)))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void excludesWeakMatchesAtLessThanTwentyPoints() {
        User weakCandidate = user(2L, true);
        weakCandidate.setBio(bio(weakCandidate, Set.of("sports"), "rock"));
        makeOtherPreferencesDifferent(weakCandidate.getBio());
        User strongCandidate = user(3L, true);
        strongCandidate.setBio(bio(strongCandidate, Set.of("music"), "pop"));
        makeOtherPreferencesDifferent(strongCandidate.getBio());
        when(userService.getAllUsersWithCompletedProfiles(1L))
                .thenReturn(List.of(weakCandidate, strongCandidate));

        List<User> recommendations = recommendationService.getRecommendations(1L);

        assertThat(recommendations).extracting(User::getId).containsExactly(3L);
    }

    private User user(Long id, boolean profileCompleted) {
        User user = new User();
        user.setId(id);
        user.setProfileCompleted(profileCompleted);
        return user;
    }

    private Bio bio(User user, Set<String> interests, String musicTaste) {
        Bio bio = new Bio();
        bio.setUser(user);
        bio.setLocation(location);
        bio.setInterests(interests);
        bio.setHobbies(Set.of("reading"));
        bio.setMusicTaste(musicTaste);
        bio.setFoodPreference("anything");
        bio.setLifestyle("balanced");
        bio.setPersonality("ambivert");
        return bio;
    }

    private void makeOtherPreferencesDifferent(Bio bio) {
        bio.setHobbies(Set.of("hiking"));
        bio.setFoodPreference("vegan");
        bio.setLifestyle("active");
        bio.setPersonality("introvert");
    }
}