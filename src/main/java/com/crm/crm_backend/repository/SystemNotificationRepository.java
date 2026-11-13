package com.crm.crm_backend.repository;

import com.crm.crm_backend.model.NotificationType;
import com.crm.crm_backend.model.SystemNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

    // Find all notifications for a specific user (including broadcast)
    @Query("SELECT n FROM SystemNotification n WHERE " +
           "(n.recipientUsername = :username OR n.recipientUsername IS NULL) " +
           "ORDER BY n.createdAt DESC")
    Page<SystemNotification> findByRecipient(@Param("username") String username, Pageable pageable);

    // Find unread notifications for a user
    @Query("SELECT n FROM SystemNotification n WHERE " +
           "(n.recipientUsername = :username OR n.recipientUsername IS NULL) " +
           "AND n.readStatus = false " +
           "ORDER BY n.createdAt DESC")
    List<SystemNotification> findUnreadByRecipient(@Param("username") String username);

    // Count unread notifications
    @Query("SELECT COUNT(n) FROM SystemNotification n WHERE " +
           "(n.recipientUsername = :username OR n.recipientUsername IS NULL) " +
           "AND n.readStatus = false")
    long countUnreadByRecipient(@Param("username") String username);

    // Find by type
    List<SystemNotification> findByType(NotificationType type);

    // Find by related entity
    List<SystemNotification> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId);

    // Mark all as read for a user
    @Modifying
    @Query("UPDATE SystemNotification n SET n.readStatus = true, n.readAt = :readAt " +
           "WHERE (n.recipientUsername = :username OR n.recipientUsername IS NULL) " +
           "AND n.readStatus = false")
    int markAllAsReadForUser(@Param("username") String username, @Param("readAt") LocalDateTime readAt);

    // Delete old read notifications (cleanup)
    @Modifying
    @Query("DELETE FROM SystemNotification n WHERE n.readStatus = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Delete expired notifications
    @Modifying
    @Query("DELETE FROM SystemNotification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);

    // Find broadcast notifications
    @Query("SELECT n FROM SystemNotification n WHERE n.recipientUsername IS NULL ORDER BY n.createdAt DESC")
    List<SystemNotification> findBroadcastNotifications();
}
