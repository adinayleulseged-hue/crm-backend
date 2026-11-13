package com.crm.crm_backend.service;

import com.crm.crm_backend.dto.NotificationDTO;
import com.crm.crm_backend.exception.ResourceNotFoundException;
import com.crm.crm_backend.model.NotificationType;
import com.crm.crm_backend.model.SystemNotification;
import com.crm.crm_backend.repository.SystemNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemNotificationService {

    private final SystemNotificationRepository notificationRepository;

    public Page<SystemNotification> getNotificationsForUser(String username, Pageable pageable) {
        return notificationRepository.findByRecipient(username, pageable);
    }

    public List<SystemNotification> getUnreadNotifications(String username) {
        return notificationRepository.findUnreadByRecipient(username);
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countUnreadByRecipient(username);
    }

    @Transactional(readOnly = true)
    public SystemNotification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    @Async
    public void createNotification(NotificationDTO notificationDTO) {
        log.info("Creating notification: {}", notificationDTO.getTitle());

        SystemNotification notification = SystemNotification.builder()
                .recipientUsername(notificationDTO.getRecipientUsername())
                .title(notificationDTO.getTitle())
                .message(notificationDTO.getMessage())
                .type(notificationDTO.getType() != null ? notificationDTO.getType() : NotificationType.INFO)
                .readStatus(false)
                .relatedEntityType(notificationDTO.getRelatedEntityType())
                .relatedEntityId(notificationDTO.getRelatedEntityId())
                .actionUrl(notificationDTO.getActionUrl())
                .priority(notificationDTO.getPriority() != null ? notificationDTO.getPriority() : "NORMAL")
                .expiresAt(notificationDTO.getExpiresAt())
                .build();

        notificationRepository.save(notification);
        log.info("Notification created successfully");
    }

    @Async
    public void broadcastNotification(String title, String message, NotificationType type) {
        log.info("Broadcasting notification to all users: {}", title);

        SystemNotification notification = SystemNotification.builder()
                .recipientUsername(null) // Broadcast to all
                .title(title)
                .message(message)
                .type(type)
                .readStatus(false)
                .priority("NORMAL")
                .build();

        notificationRepository.save(notification);
        log.info("Broadcast notification created successfully");
    }

    public void markAsRead(Long id, String username) {
        log.info("Marking notification {} as read for user: {}", id, username);

        SystemNotification notification = getNotificationById(id);

        // Verify user has access to this notification
        if (notification.getRecipientUsername() != null && 
            !notification.getRecipientUsername().equals(username)) {
            throw new IllegalArgumentException("User does not have access to this notification");
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("Notification marked as read: {}", id);
    }

    public int markAllAsRead(String username) {
        log.info("Marking all notifications as read for user: {}", username);

        int count = notificationRepository.markAllAsReadForUser(username, LocalDateTime.now());

        log.info("Marked {} notifications as read", count);
        return count;
    }

    public void deleteNotification(Long id) {
        log.info("Deleting notification with ID: {}", id);

        SystemNotification notification = getNotificationById(id);
        notificationRepository.delete(notification);

        log.info("Notification deleted successfully: {}", id);
    }

    @Transactional
    public int cleanupOldNotifications(int daysToKeep) {
        log.info("Cleaning up read notifications older than {} days", daysToKeep);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        int deleted = notificationRepository.deleteOldReadNotifications(cutoffDate);

        log.info("Deleted {} old read notifications", deleted);
        return deleted;
    }

    @Transactional
    public int deleteExpiredNotifications() {
        log.info("Deleting expired notifications");

        int deleted = notificationRepository.deleteExpiredNotifications(LocalDateTime.now());

        log.info("Deleted {} expired notifications", deleted);
        return deleted;
    }

    @Transactional(readOnly = true)
    public List<SystemNotification> getNotificationsByRelatedEntity(String entityType, Long entityId) {
        return notificationRepository.findByRelatedEntityTypeAndRelatedEntityId(entityType, entityId);
    }

    // Helper methods for creating specific notification types

    @Async
    public void notifyLeadCreated(String username, Long leadId, String leadName) {
        NotificationDTO dto = NotificationDTO.builder()
                .recipientUsername(username)
                .title("New Lead Created")
                .message("New lead '" + leadName + "' has been created and assigned to you.")
                .type(NotificationType.LEAD_CREATED)
                .relatedEntityType("LEAD")
                .relatedEntityId(leadId)
                .actionUrl("/crm/leads/" + leadId)
                .priority("NORMAL")
                .build();

        createNotification(dto);
    }

    @Async
    public void notifyOrderApprovalRequired(String username, Long orderId, String orderNumber) {
        NotificationDTO dto = NotificationDTO.builder()
                .recipientUsername(username)
                .title("Order Approval Required")
                .message("Sales Order " + orderNumber + " requires your approval.")
                .type(NotificationType.ORDER_APPROVAL)
                .relatedEntityType("ORDER")
                .relatedEntityId(orderId)
                .actionUrl("/dashboard/sales-manager")
                .priority("HIGH")
                .build();

        createNotification(dto);
    }

    @Async
    public void notifyInvoicePaid(String username, Long invoiceId, String invoiceNumber) {
        NotificationDTO dto = NotificationDTO.builder()
                .recipientUsername(username)
                .title("Invoice Payment Received")
                .message("Invoice " + invoiceNumber + " has been paid.")
                .type(NotificationType.INVOICE_PAID)
                .relatedEntityType("INVOICE")
                .relatedEntityId(invoiceId)
                .actionUrl("/crm/invoices/" + invoiceId)
                .priority("NORMAL")
                .build();

        createNotification(dto);
    }

    @Async
    public void notifyInvoiceRejected(com.crm.crm_backend.model.entity.Invoice invoice, String reason) {
        if (invoice.getCustomer() == null || invoice.getCustomer().getEmail() == null) {
            log.warn("Cannot notify invoice rejection - customer or email is null for invoice {}", invoice.getInvoiceNumber());
            return;
        }

        NotificationDTO dto = NotificationDTO.builder()
                .recipientUsername(invoice.getCustomer().getEmail())
                .title("Invoice Rejected")
                .message("Invoice " + invoice.getInvoiceNumber() + " has been rejected by Finance. Reason: " + reason)
                .type(NotificationType.INVOICE_REJECTED)
                .relatedEntityType("INVOICE")
                .relatedEntityId(invoice.getId())
                .actionUrl("/customer/dashboard")
                .priority("HIGH")
                .build();

        createNotification(dto);
    }

    @Async
    public void notifyInvoiceApproved(com.crm.crm_backend.model.entity.Invoice invoice) {
        if (invoice.getCustomer() == null || invoice.getCustomer().getEmail() == null) {
            log.warn("Cannot notify invoice approval - customer or email is null for invoice {}", invoice.getInvoiceNumber());
            return;
        }

        NotificationDTO dto = NotificationDTO.builder()
                .recipientUsername(invoice.getCustomer().getEmail())
                .title("Invoice Approved")
                .message("Invoice " + invoice.getInvoiceNumber() + " has been approved by Finance. Payment status: PENDING")
                .type(NotificationType.INVOICE_APPROVED)
                .relatedEntityType("INVOICE")
                .relatedEntityId(invoice.getId())
                .actionUrl("/customer/dashboard")
                .priority("NORMAL")
                .build();

        createNotification(dto);
    }
}
