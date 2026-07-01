package com.service;

import com.model.Connection;
import com.model.Connection.ConnectionStatus;
import com.model.User;
import com.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConnectionService {

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserService userService;

    /**
     * Send a connection request
     */
    public Connection sendConnectionRequest(Long requesterId, Long receiverId) {
        User requester = userService.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Check if connection already exists
        Optional<Connection> existing = connectionRepository
                .findConnectionBetweenUsers(requester, receiver);

        if (existing.isPresent()) {
            throw new RuntimeException("Connection already exists");
        }

        // Create new connection request
        Connection connection = new Connection();
        connection.setRequester(requester);
        connection.setReceiver(receiver);
        connection.setStatus(ConnectionStatus.PENDING);
        connection.setRequestedAt(LocalDateTime.now());

        return connectionRepository.save(connection);
    }

    /**
     * Accept a connection request
     */
    public Connection acceptConnectionRequest(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        // Verify that the current user is the receiver
        if (!connection.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to accept this request");
        }

        // Verify status is PENDING
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new RuntimeException("Connection request is not pending");
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        connection.setRespondedAt(LocalDateTime.now());

        return connectionRepository.save(connection);
    }

    /**
     * Reject a connection request
     */
    public Connection rejectConnectionRequest(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        // Verify that the current user is the receiver
        if (!connection.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to reject this request");
        }

        // Verify status is PENDING
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new RuntimeException("Connection request is not pending");
        }

        connection.setStatus(ConnectionStatus.REJECTED);
        connection.setRespondedAt(LocalDateTime.now());

        return connectionRepository.save(connection);
    }

    /**
     * Disconnect from a user
     */
    public void disconnect(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        // Verify that the current user is part of this connection
        if (!connection.getRequester().getId().equals(userId) &&
                !connection.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You are not part of this connection");
        }

        // Verify status is ACCEPTED
        if (connection.getStatus() != ConnectionStatus.ACCEPTED) {
            throw new RuntimeException("Connection is not active");
        }

        connection.setStatus(ConnectionStatus.DISCONNECTED);
        connection.setRespondedAt(LocalDateTime.now());

        connectionRepository.save(connection);
    }

    /**
     * Get all pending connection requests for a user
     */
    public List<Connection> getPendingRequests(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return connectionRepository.findByUserAndStatus(user, ConnectionStatus.PENDING)
                .stream()
                .filter(c -> c.getReceiver().getId().equals(userId)) // Only requests received
                .collect(Collectors.toList());
    }

    /**
     * Get all accepted connections for a user
     */
    public List<Connection> getConnections(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return connectionRepository.findAcceptedConnectionsForUser(user);
    }

    /**
     * Get connected user IDs (for API response)
     */
    public List<Long> getConnectedUserIds(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return connectionRepository.findAcceptedConnectionsForUser(user)
                .stream()
                .map(c -> c.getRequester().equals(user) ?
                        c.getReceiver().getId() : c.getRequester().getId())
                .collect(Collectors.toList());
    }

    /**
     * Check if two users are connected
     */
    public boolean areUsersConnected(Long userId1, Long userId2) {
        User user1 = userService.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User1 not found"));

        User user2 = userService.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User2 not found"));

        Optional<Connection> connection = connectionRepository
                .findConnectionBetweenUsers(user1, user2);

        return connection.isPresent() &&
                connection.get().getStatus() == ConnectionStatus.ACCEPTED;
    }
}