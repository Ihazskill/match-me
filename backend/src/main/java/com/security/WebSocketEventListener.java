package com.security;

import com.model.User;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        userService.findByEmail(principal.getName()).ifPresent(user -> {
            userService.updateOnlineStatus(user.getId(), true);
            broadcastOnlineStatus(user, true);
        });
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) return;

        userService.findByEmail(principal.getName()).ifPresent(user -> {
            userService.updateOnlineStatus(user.getId(), false);
            broadcastOnlineStatus(user, false);
        });
    }

    /**
     * Notifies the topic /topic/online/{userId} with the new status.
     * The frontend can subscribe to this for any user they care about
     * (e.g. the person they're chatting with).
     */
    private void broadcastOnlineStatus(User user, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/online/" + user.getId(),
                new OnlineStatusUpdate(user.getId(), online)
        );
    }

    public record OnlineStatusUpdate(Long userId, boolean online) {}
}