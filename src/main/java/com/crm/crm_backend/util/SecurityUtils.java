package com.crm.crm_backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility class for security-related operations
 */
public class SecurityUtils {

    /**
     * Get the ID of the currently authenticated user
     * Returns null if no user is authenticated or user details are unavailable
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        // If using custom UserDetails with getId() method
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // Try to parse username as ID if it's numeric
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                // Username is not numeric, return null
                return null;
            }
        }
        
        // If principal is directly a Long
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        // Default: return null
        return null;
    }

    /**
     * Get the username of the currently authenticated user
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        return principal.toString();
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
