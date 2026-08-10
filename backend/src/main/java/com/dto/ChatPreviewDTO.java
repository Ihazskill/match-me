package com.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatPreviewDTO {
    private Long chatId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfilePicture;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private boolean otherUserOnline;
}