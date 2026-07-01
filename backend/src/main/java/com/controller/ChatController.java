
package com.controller;

import com.dto.ChatPreviewDTO;
import com.dto.MessageDTO;
import com.model.Chat;
import com.model.Message;
import com.model.Profile;
import com.model.User;
import com.service.MessageService;
import com.service.ProfileService;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    /**
     * GET /api/chats
     * All chats for the authenticated user, most recent first.
     */
    @GetMapping
    public ResponseEntity<?> getChats(Authentication authentication) {
        User me = resolveUser(authentication);
        List<Chat> chats = messageService.getChatsForUser(me.getId());

        List<ChatPreviewDTO> previews = chats.stream().map(chat -> {
            User other = chat.getUser1().getId().equals(me.getId())
                    ? chat.getUser2()
                    : chat.getUser1();

            Profile otherProfile = profileService.getProfile(other.getId()).orElse(null);
            String otherName = otherProfile != null
                    ? otherProfile.getFirstName() + " " + otherProfile.getLastName()
                    : "Unknown";
            String otherPicture = otherProfile != null
                    ? otherProfile.getProfilePictureUrl()
                    : null;

            // Get the most recent message content for the preview
            Page<Message> lastPage = messageService.getMessages(chat.getId(), me.getId(), 0, 1);
            String lastMessageContent = lastPage.hasContent()
                    ? lastPage.getContent().get(0).getContent()
                    : null;

            long unread = messageService.countUnreadMessagesInChat(chat.getId(), me.getId());

            return new ChatPreviewDTO(
                    chat.getId(),
                    other.getId(),
                    otherName,
                    otherPicture,
                    lastMessageContent,
                    chat.getLastMessageAt(),
                    unread,
                    other.isOnline()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(previews);
    }

    /**
     * POST /api/chats/with/{userId}
     * Start or resume a chat with a connected user.
     * Returns { "chatId": 123 }
     */
    @PostMapping("/with/{userId}")
    public ResponseEntity<?> startOrGetChat(
            @PathVariable Long userId,
            Authentication authentication) {

        try {
            User me = resolveUser(authentication);
            Chat chat = messageService.startOrGetChat(me.getId(), userId);
            return ResponseEntity.ok(Map.of("chatId", chat.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/chats/{chatId}/messages?page=0&size=20
     * Paginated messages, most recent first.
     * Also marks received messages as read.
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        try {
            User me = resolveUser(authentication);
            Page<Message> messagePage = messageService.getMessages(chatId, me.getId(), page, size);
            messageService.markMessagesAsRead(chatId, me.getId());
            return ResponseEntity.ok(messagePage.map(this::toDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/chats/{chatId}/messages
     * Send a message. Body: { "content": "Hello!" }
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        try {
            User me = resolveUser(authentication);
            Message message = messageService.sendMessage(chatId, me.getId(), body.get("content"));
            return ResponseEntity.ok(toDTO(message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/chats/unread
     * Total unread count across all chats — used for the global notification badge.
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        User me = resolveUser(authentication);
        long count = messageService.countUnreadMessages(me.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private User resolveUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

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