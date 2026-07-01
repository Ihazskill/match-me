package com.repository;

import com.model.Connection;
import com.model.Connection.ConnectionStatus;
import com.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    @Query("SELECT c FROM Connection c WHERE " +
            "(c.requester = :user OR c.receiver = :user) " +
            "AND c.status = :status")
    List<Connection> findByUserAndStatus(
            @Param("user") User user,
            @Param("status") ConnectionStatus status
    );

    @Query("SELECT c FROM Connection c WHERE " +
            "((c.requester = :user1 AND c.receiver = :user2) OR " +
            "(c.requester = :user2 AND c.receiver = :user1))")
    Optional<Connection> findConnectionBetweenUsers(
            @Param("user1") User user1,
            @Param("user2") User user2
    );

    @Query("SELECT c FROM Connection c WHERE " +
            "(c.requester = :user OR c.receiver = :user) " +
            "AND c.status = 'ACCEPTED'")
    List<Connection> findAcceptedConnectionsForUser(@Param("user") User user);
}