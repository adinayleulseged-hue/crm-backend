package com.crm.crm_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_notifications", indexes = {
    @Index(name = "idx_recipient_read_status", columnList = "recipient_username, read_status"),
    @Index(name = "idx_created_at", columnList = "created_at DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_username", length = 100)
    private String recipientUsername; // Null means broadcast to all users

    @NotBlank(message = "Notification title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Notification message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type = NotificationType.INFO;

    @Column(name = "read_status", nullable = false)
    private Boolean readStatus = false;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // e.g., "LEAD", "ORDER", "INVOICE"

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "action_url", length = 500)
    private String actionUrl; // URL to navigate when notification is clicked

    @Column(name = "priority", length = 20)
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Business methods
    public void markAsRead() {
        this.readStatus = true;
        this.readAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isBroadcast() {
        return recipientUsername == null || recipientUsername.trim().isEmpty();
    }

    @PrePersist
    protected void onCreate() {
        if (readStatus == null) {
            readStatus = false;
        }
        if (type == null) {
            type = NotificationType.INFO;
        }
    }
}
