package com.controller;

import com.dto.ChatPreviewDTO;
import com.dto.MessageDTO;
import com.model.User;
import com.service.MessageService;
import com.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(
            MessageService messageService,
            UserService userService,
            SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/api/chats/with/{userId}")
    @ResponseBody
    public ResponseEntity<ChatPreviewDTO> getOrCreateChat(
            @PathVariable Long userId,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.getOrCreateChat(currentUser(authentication).getId(), userId));
    }

    @GetMapping("/api/chats")
    @ResponseBody
    public ResponseEntity<List<ChatPreviewDTO>> getChats(Authentication authentication) {
        return ResponseEntity.ok(messageService.getChats(currentUser(authentication).getId()));
    }

    @GetMapping("/api/chats/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<Page<MessageDTO>> getMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Authentication authentication) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ResponseEntity.ok(messageService.getMessages(
                chatId,
                currentUser(authentication).getId(),
                PageRequest.of(Math.max(page, 0), safeSize)
        ));
    }

    @PostMapping("/api/chats/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable Long chatId,
            @RequestBody SendMessageRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.sendMessage(
                chatId,
                currentUser(authentication).getId(),
                request.getContent()
        ));
    }

    @MessageMapping("/chat.send")
    public void sendMessage(SocketMessageRequest request, Principal principal) {
        User sender = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        messageService.sendMessage(request.getChatId(), sender.getId(), request.getContent());
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingRequest request, Principal principal) {
        User sender = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User receiver = messageService.getOtherUser(request.getChatId(), sender.getId());
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/typing", new TypingEvent(
                request.getChatId(),
                sender.getId(),
                request.isTyping()
        ));
    }

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Getter
    @Setter
    public static class SendMessageRequest {
        private String content;
    }

    @Getter
    @Setter
    public static class SocketMessageRequest extends SendMessageRequest {
        private Long chatId;
    }

    @Getter
    @Setter
    public static class TypingRequest {
        private Long chatId;
        private boolean typing;
    }

    public record TypingEvent(Long chatId, Long userId, boolean typing) {
    }
}