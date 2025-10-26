/*
 * SECURITY FIX #1 & #5: Broken Object Level Authorization (BOLA) & 
 * Broken Function Level Authorization (BFLA)
 * 
 * VULNERABILITY: Users could access or modify resources belonging to other users
 * by manipulating object IDs in API requests. No proper authorization checks.
 * 
 * FIX IMPLEMENTED:
 * - Added UserContextService to maintain authenticated user context
 * - Validates user ownership before allowing access to resources
 * - Implements role-based access control (RBAC)
 * - Checks permissions at both object and function levels
 * 
 * IMPACT: Ensures users can only access their own data and authorized functions
 */

package edu.nu.owaspapivulnlab.service;

import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SECURITY FIX: API1:2023 - Broken Object Level Authorization (BOLA/IDOR)
 * 
 * This service provides secure user context management to prevent unauthorized
 * access to resources. It implements proper authorization checks to ensure
 * users can only access their own resources and admin functions are properly
 * protected.
 * 
 * Key Security Features:
 * - User context validation from JWT tokens
 * - Resource ownership verification
 * - Admin role validation
 * - Protection against IDOR attacks
 * 
 * OWASP Recommendation: Implement proper authorization checks for all resources
 * 
 * @author Security Team
 * @version 1.0
 * @since 2024-12
 */
@Service
public class UserContextService {
    
    private final AppUserRepository userRepository;
    
    public UserContextService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * SECURITY FIX: Get current authenticated user from security context
     * 
     * This method safely extracts the current user from the Spring Security
     * context, ensuring proper authentication state validation.
     * 
     * @return Optional containing current user or empty if not authenticated
     */
    public Optional<AppUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("UserContextService: Authentication = " + (auth != null ? auth.getName() : "null"));
        System.out.println("UserContextService: Authenticated = " + (auth != null && auth.isAuthenticated()));
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            System.out.println("UserContextService: Looking up user: " + username);
            
            Optional<AppUser> user = userRepository.findByUsername(username);
            System.out.println("UserContextService: User found = " + user.isPresent());
            if (user.isPresent()) {
                System.out.println("UserContextService: User ID = " + user.get().getId());
            }
            
            return user;
        }
        return Optional.empty();
    }
    
    /**
     * SECURITY FIX: Get current user ID for authorization checks
     * 
     * This method provides the current user's ID for resource ownership
     * validation. Returns null if user is not authenticated.
     * 
     * @return Current user ID or null if not authenticated
     */
    public Long getCurrentUserId() {
        try {
            return getCurrentUser().map(AppUser::getId).orElse(null);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error getting current user ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * SECURITY FIX: Verify if user is accessing their own resource
     * 
     * This method implements the core BOLA/IDOR prevention by ensuring
     * users can only access resources they own. This prevents unauthorized
     * access to other users' data.
     * 
     * @param userId The ID of the resource being accessed
     * @return true if user owns the resource, false otherwise
     */
    public boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }
    
    /**
     * SECURITY FIX: Verify admin privileges for function-level authorization
     * 
     * This method ensures that only users with admin privileges can access
     * administrative functions. This prevents privilege escalation attacks
     * and unauthorized access to sensitive operations.
     * 
     * @return true if user has admin privileges, false otherwise
     */
    public boolean isAdmin() {
        return getCurrentUser()
                .map(user -> "ADMIN".equals(user.getRole()) && user.isAdmin())
                .orElse(false);
    }
}
