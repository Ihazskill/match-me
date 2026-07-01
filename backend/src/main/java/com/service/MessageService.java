
package com.service;

import com.model.Chat;
import com.model.Message;
import com.model.User;
import com.repository.ChatRepository;
import com.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService connectionService;

    /** Get all chats for a user, most recent first. */
    public List<Chat> getChatsForUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatRepository.findByUser(user);
    }

    /**
     * Start or resume a chat between two connected users.
     * Creates the chat if it does not exist yet.
     */
    public Chat startOrGetChat(Long currentUserId, Long otherUserId) {
        User currentUser = userService.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User otherUser = userService.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Other user not found"));

        if (!connectionService.areUsersConnected(currentUserId, otherUserId)) {
            throw new RuntimeException("You must be connected with this user to start a chat");
        }

        Optional<Chat> existing = chatRepository.findBetweenUsers(currentUser, otherUser);
        if (existing.isPresent()) return existing.get();

        Chat chat = new Chat();
        chat.setUser1(currentUser);
        chat.setUser2(otherUser);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setLastMessageAt(LocalDateTime.now());
        return chatRepository.save(chat);
    }

    /** Get a chat by ID, asserting the requesting user is a participant. */
    public Chat getChatById(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        assertParticipant(chat, userId);
        return chat;
    }

    /** Get paginated messages for a chat (most recent first). */
    public Page<Message> getMessages(Long chatId, Long userId, int page, int size) {
        Chat chat = getChatById(chatId, userId);
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByChatOrderBySentAtDesc(chat, pageable);
    }

    /** Send a message in a chat. */
    public Message sendMessage(Long chatId, Long senderId, String content) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("Message content cannot be empty");
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        assertParticipant(chat, senderId);

        User sender = userService.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = chat.getUser1().getId().equals(senderId)
                ? chat.getUser2()
                : chat.getUser1();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);

        Message saved = messageRepository.save(message);

        chat.setLastMessageAt(saved.getSentAt());
        chatRepository.save(chat);

        return saved;
    }

    /** Mark all messages in a chat as read for the given user. */
    public void markMessagesAsRead(Long chatId, Long userId) {
        Chat chat = getChatById(chatId, userId);
        Pageable all = PageRequest.of(0, Integer.MAX_VALUE);
        messageRepository.findByChatOrderBySentAtDesc(chat, all)
                .getContent()
                .stream()
                .filter(m -> m.getReceiver().getId().equals(userId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    m.setReadAt(LocalDateTime.now());
                    messageRepository.save(m);
                });
    }

    /** Count total unread messages for a user across all chats. */
    public long countUnreadMessages(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.countUnreadMessagesForUser(user);
    }

    /** Count unread messages in a specific chat for a user. */
    public long countUnreadMessagesInChat(Long chatId, Long userId) {
        Chat chat = getChatById(chatId, userId);
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.countUnreadMessagesInChat(chat, user);
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private void assertParticipant(Chat chat, Long userId) {
        if (!chat.getUser1().getId().equals(userId) && !chat.getUser2().getId().equals(userId)) {
            throw new RuntimeException("You are not a participant in this chat");
        }
    }
}