package com.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dismissed_users")
public class DismissedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dismissed_by_id", nullable = false)
    private User dismissedBy;

    @ManyToOne
    @JoinColumn(name = "dismissed_user_id", nullable = false)
    private User dismissedUser;

    @Column(nullable = false)
    private LocalDateTime dismissedAt = LocalDateTime.now();
}