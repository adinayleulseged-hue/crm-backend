package com.crm.crm_backend.dto;

import com.crm.crm_backend.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    private String recipientUsername;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private NotificationType type;

    private Boolean readStatus;

    private String relatedEntityType;

    private Long relatedEntityId;

    private String actionUrl;

    private String priority;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    private LocalDateTime expiresAt;
}
