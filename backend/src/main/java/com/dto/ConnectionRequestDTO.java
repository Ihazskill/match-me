package com.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDTO {
    private Long connectionId;
    private Long requesterId;
    private String requesterName;
    private String requesterProfilePicture;
    private LocalDateTime requestedAt;
}