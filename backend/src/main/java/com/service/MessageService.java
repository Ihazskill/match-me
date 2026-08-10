package com.service;

import com.dto.ChatPreviewDTO;
import com.dto.MessageDTO;
import com.model.Chat;
import com.model.Message;
import com.model.Profile;
import com.model.User;
import com.repository.ChatRepository;
import com.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MessageService {

	private final ChatRepository chatRepository;
	private final MessageRepository messageRepository;
	private final UserService userService;
	private final ConnectionService connectionService;
	private final SimpMessagingTemplate messagingTemplate;

	public MessageService(
			ChatRepository chatRepository,
			MessageRepository messageRepository,
			UserService userService,
			ConnectionService connectionService,
			SimpMessagingTemplate messagingTemplate) {
		this.chatRepository = chatRepository;
		this.messageRepository = messageRepository;
		this.userService = userService;
		this.connectionService = connectionService;
		this.messagingTemplate = messagingTemplate;
	}

	public ChatPreviewDTO getOrCreateChat(Long userId, Long otherUserId) {
		if (!connectionService.areUsersConnected(userId, otherUserId)) {
			throw new RuntimeException("Users must be connected to chat");
		}
		User user = requireUser(userId);
		User otherUser = requireUser(otherUserId);
		Chat chat = chatRepository.findBetweenUsers(user, otherUser).orElseGet(() -> {
			Chat created = new Chat();
			created.setUser1(user);
			created.setUser2(otherUser);
			return chatRepository.save(created);
		});
		return toPreview(chat, user);
	}

	public List<ChatPreviewDTO> getChats(Long userId) {
		User user = requireUser(userId);
		return chatRepository.findByUser(user).stream()
				.map(chat -> toPreview(chat, user))
				.toList();
	}

	public Page<MessageDTO> getMessages(Long chatId, Long userId, Pageable pageable) {
		User user = requireUser(userId);
		Chat chat = requireParticipant(chatId, userId);
		List<Message> unreadMessages = messageRepository.findByChatAndReceiverAndIsReadFalse(chat, user);
		LocalDateTime readAt = LocalDateTime.now();
		unreadMessages.forEach(message -> {
			message.setRead(true);
			message.setReadAt(readAt);
		});
		messageRepository.saveAll(unreadMessages);
		return messageRepository.findByChatOrderBySentAtDesc(chat, pageable).map(this::toMessageDTO);
	}

	public MessageDTO sendMessage(Long chatId, Long senderId, String content) {
		String trimmedContent = content == null ? "" : content.trim();
		if (trimmedContent.isEmpty()) {
			throw new RuntimeException("Message cannot be empty");
		}
		Chat chat = requireParticipant(chatId, senderId);
		User sender = requireUser(senderId);
		User receiver = getOtherUser(chat, senderId);
		if (!connectionService.areUsersConnected(senderId, receiver.getId())) {
			throw new RuntimeException("Users must be connected to chat");
		}

		Message message = new Message();
		message.setChat(chat);
		message.setSender(sender);
		message.setReceiver(receiver);
		message.setContent(trimmedContent);
		message.setSentAt(LocalDateTime.now());
		Message saved = messageRepository.save(message);

		chat.setLastMessageAt(saved.getSentAt());
		chatRepository.save(chat);

		MessageDTO dto = toMessageDTO(saved);
		messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/messages", dto);
		messagingTemplate.convertAndSendToUser(sender.getEmail(), "/queue/messages", dto);
		return dto;
	}

	public User getOtherUser(Long chatId, Long userId) {
		return getOtherUser(requireParticipant(chatId, userId), userId);
	}

	private User getOtherUser(Chat chat, Long userId) {
		return chat.getUser1().getId().equals(userId) ? chat.getUser2() : chat.getUser1();
	}

	private Chat requireParticipant(Long chatId, Long userId) {
		Chat chat = chatRepository.findById(chatId)
				.orElseThrow(() -> new RuntimeException("Chat not found"));
		if (!chat.getUser1().getId().equals(userId) && !chat.getUser2().getId().equals(userId)) {
			throw new RuntimeException("Chat not found");
		}
		return chat;
	}

	private User requireUser(Long userId) {
		return userService.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private ChatPreviewDTO toPreview(Chat chat, User viewer) {
		User otherUser = getOtherUser(chat, viewer.getId());
		Profile profile = otherUser.getProfile();
		Message lastMessage = chat.getMessages().isEmpty() ? null : chat.getMessages().get(0);
		String name = profile == null ? "Unknown user" : profile.getFirstName() + " " + profile.getLastName();
		return new ChatPreviewDTO(
				chat.getId(),
				otherUser.getId(),
				name,
				profile == null ? null : profile.getProfilePictureUrl(),
				lastMessage == null ? null : lastMessage.getContent(),
				chat.getLastMessageAt(),
				messageRepository.countUnreadMessagesInChat(chat, viewer),
				otherUser.isOnline()
		);
	}

	private MessageDTO toMessageDTO(Message message) {
		return new MessageDTO(
				message.getId(),
			message.getChat().getId(),
				message.getSender().getId(),
				message.getReceiver().getId(),
				message.getContent(),
				message.getSentAt(),
				message.isRead()
		);
	}
}
