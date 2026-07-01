package com.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bios")
public class Bio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Biographical data points for matching (minimum 5 required)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bio_interests", joinColumns = @JoinColumn(name = "bio_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bio_hobbies", joinColumns = @JoinColumn(name = "bio_id"))
    @Column(name = "hobby")
    private Set<String> hobbies = new HashSet<>();

    @Column
    private String musicTaste;

    @Column
    private String foodPreference;

    @Column
    private String travelStyle;

    @Column
    private String lifestyle;

    @Column
    private String personality;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private com.model.Location location;

    // What they're looking for
    @Column(columnDefinition = "TEXT")
    private String lookingFor;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bio_seeking_interests", joinColumns = @JoinColumn(name = "bio_id"))
    @Column(name = "seeking_interest")
    private Set<String> seekingInterests = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "seeking_location_id")
    private com.model.Location seekingLocation;

    // Preference weights (for recommendation scoring)
    @Column
    private Integer interestWeight = 20;

    @Column
    private Integer hobbyWeight = 20;

    @Column
    private Integer musicWeight = 15;

    @Column
    private Integer foodWeight = 15;

    @Column
    private Integer lifestyleWeight = 15;

    @Column
    private Integer personalityWeight = 15;
}