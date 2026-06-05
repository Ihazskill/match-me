package com.repository;

import com.model.Chat;
import com.model.Message;
import com.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatOrderBySentAtDesc(Chat chat, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "m.receiver = :user AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("user") User user);

    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "m.chat = :chat AND m.receiver = :user AND m.isRead = false")
    long countUnreadMessagesInChat(
            @Param("chat") Chat chat,
            @Param("user") User user
    );
}