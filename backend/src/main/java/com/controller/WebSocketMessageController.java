package com.controller;

import com.dto.MessageDTO;
import com.model.Message;
import com.service.MessageService;
import com.service.UserService;
import com.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class WebSocketMessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    /**
     * Client sends to: /app/chat/{chatId}
     *
     * Persists the message and pushes it in real-time to:
     *  - /user/{receiverEmail}/queue/messages  → recipient sees the new message
     *  - /user/{senderEmail}/queue/messages    → sender's own UI updates
     *
     * Also notifies the recipient's unread badge:
     *  - /user/{receiverEmail}/queue/unread
     */
    @MessageMapping("/chat/{chatId}")
    public void sendMessage(
            @DestinationVariable Long chatId,
            @Payload IncomingMessage payload,
            Principal principal) {

        String senderEmail = principal.getName();
        User sender = userService.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Persist via the same service used by the REST endpoint
        Message saved = messageService.sendMessage(chatId, sender.getId(), payload.getContent());

        MessageDTO dto = toDTO(saved);

        // Push to the receiver
        String receiverEmail = saved.getReceiver().getEmail();
        messagingTemplate.convertAndSendToUser(receiverEmail, "/queue/messages", dto);

        // Push back to the sender so their chat view updates immediately
        messagingTemplate.convertAndSendToUser(senderEmail, "/queue/messages", dto);

        // Notify receiver to refresh their unread count / reorder chat list
        messagingTemplate.convertAndSendToUser(receiverEmail, "/queue/unread",
                new UnreadNotification(chatId, saved.getChat().getLastMessageAt()));
    }

    /**
     * Client sends to: /app/chat/{chatId}/typing
     *
     * Broadcasts a typing indicator to the other participant.
     * Destination: /user/{receiverEmail}/queue/typing
     */
    @MessageMapping("/chat/{chatId}/typing")
    public void typing(
            @DestinationVariable Long chatId,
            Principal principal) {

        String senderEmail = principal.getName();
        User sender = userService.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Resolve the chat to find the other participant
        com.model.Chat chat = messageService.getChatById(chatId, sender.getId());
        User receiver = chat.getUser1().getId().equals(sender.getId())
                ? chat.getUser2()
                : chat.getUser1();

        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/typing",
                new TypingIndicator(chatId, sender.getId(), senderEmail)
        );
    }

    // ─── Payload / response classes ──────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class IncomingMessage {
        private String content;
    }

    @Getter
    @NoArgsConstructor
    public static class TypingIndicator {
        private Long chatId;
        private Long senderId;
        private String senderEmail;

        public TypingIndicator(Long chatId, Long senderId, String senderEmail) {
            this.chatId = chatId;
            this.senderId = senderId;
            this.senderEmail = senderEmail;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UnreadNotification {
        private Long chatId;
        private LocalDateTime lastMessageAt;

        public UnreadNotification(Long chatId, LocalDateTime lastMessageAt) {
            this.chatId = chatId;
            this.lastMessageAt = lastMessageAt;
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private MessageDTO toDTO(Message m) {
        return new MessageDTO(
                m.getId(),
                m.getSender().getId(),
                m.getReceiver().getId(),
                m.getContent(),
                m.getSentAt(),
                m.isRead()
        );
    }
}