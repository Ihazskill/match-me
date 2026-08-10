package com.service;

import com.model.User;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class PresenceEventListener {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceEventListener(UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void connected(SessionConnectedEvent event) {
        updatePresence(event.getUser(), true);
    }

    @EventListener
    public void disconnected(SessionDisconnectEvent event) {
        updatePresence(event.getUser(), false);
    }

    private void updatePresence(Principal principal, boolean online) {
        if (principal == null) {
            return;
        }
        userService.findByEmail(principal.getName()).ifPresent(user -> {
            userService.updateOnlineStatus(user.getId(), online);
            messagingTemplate.convertAndSend("/topic/presence", new PresenceEvent(user.getId(), online));
        });
    }

    public record PresenceEvent(Long userId, boolean online) {
    }
}