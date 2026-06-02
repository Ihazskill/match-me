package com.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean profileCompleted = false;

    @Column(nullable = false)
    private boolean online = false;

    @Column
    private LocalDateTime lastSeen;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private com.model.Profile profile;  // Explicit package to avoid conflict with Spring's Profile

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Bio bio;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<com.model.Connection> sentConnections = new HashSet<>();  // Explicit package to avoid conflict with java.sql.Connection

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<com.model.Connection> receivedConnections = new HashSet<>();

    @OneToMany(mappedBy = "dismissedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DismissedUser> dismissedUsers = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}