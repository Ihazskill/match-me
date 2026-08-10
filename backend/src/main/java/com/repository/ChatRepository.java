package com.repository;

import com.model.Chat;
import com.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user OR c.user2 = :user) ORDER BY c.lastMessageAt DESC")
    List<Chat> findByUser(@Param("user") User user);

    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    Optional<Chat> findBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
}