package com.crm.crm_backend.controller;

import com.crm.crm_backend.common.ApiResponse;
import com.crm.crm_backend.dto.NotificationDTO;
import com.crm.crm_backend.model.SystemNotification;
import com.crm.crm_backend.service.SystemNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final SystemNotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<SystemNotification>>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String username = authentication.getName();
        log.info("GET /api/notifications - user: {}, page: {}, size: {}", username, page, size);

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SystemNotification> notifications = notificationService.getNotificationsForUser(username, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SystemNotification>>> getUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        log.info("GET /api/notifications/unread - user: {}", username);

        List<SystemNotification> notifications = notificationService.getUnreadNotifications(username);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        log.info("GET /api/notifications/unread-count - user: {}", username);

        long count = notificationService.getUnreadCount(username);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SystemNotification>> getNotificationById(@PathVariable Long id) {
        log.info("GET /api/notifications/{}", id);

        SystemNotification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        log.info("POST /api/notifications - Creating notification: {}", notificationDTO.getTitle());

        notificationService.createNotification(notificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created successfully", (Void)null));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("PATCH /api/notifications/{}/read - user: {}", id, username);

        notificationService.markAsRead(id, username);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", (Void)null));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        log.info("PATCH /api/notifications/read-all - user: {}", username);

        int count = notificationService.markAllAsRead(username);
        Map<String, Integer> response = new HashMap<>();
        response.put("markedCount", count);

        return ResponseEntity.ok(ApiResponse.success(count + " notifications marked as read", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        log.info("DELETE /api/notifications/{}", id);

        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", (Void)null));
    }

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> cleanupOldNotifications(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        log.info("DELETE /api/notifications/cleanup - days to keep: {}", daysToKeep);

        int deleted = notificationService.cleanupOldNotifications(daysToKeep);
        Map<String, Integer> response = new HashMap<>();
        response.put("deletedCount", deleted);

        return ResponseEntity.ok(ApiResponse.success(deleted + " old notifications deleted", response));
    }
}
