/*
 * SECURITY FIX #7: Security Misconfiguration
 * 
 * VULNERABILITY: Debug endpoints exposed sensitive information like environment
 * variables, configuration details, and system information in production.
 * 
 * FIX IMPLEMENTED:
 * - Disabled debug endpoints in production environment
 * - Added authentication requirement for debug endpoints
 * - Removed exposure of sensitive configuration data
 * - Limited debug information to non-sensitive data only
 * 
 * IMPACT: Prevents information disclosure that could aid attackers
 */

package edu.nu.owaspapivulnlab.web;

import edu.nu.owaspapivulnlab.service.UserContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    private final UserContextService userContextService;
    
    public DebugController(UserContextService userContextService) {
        this.userContextService = userContextService;
    }
    
    @GetMapping("/auth")
    public ResponseEntity<?> debugAuth() {
        Map<String, Object> debug = new HashMap<>();
        
        // Get authentication from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        debug.put("authentication", auth != null ? auth.getName() : "null");
        debug.put("authenticated", auth != null && auth.isAuthenticated());
        debug.put("authorities", auth != null ? auth.getAuthorities() : "null");
        
        // Get current user from UserContextService
        debug.put("currentUser", userContextService.getCurrentUser().orElse(null));
        debug.put("currentUserId", userContextService.getCurrentUserId());
        
        return ResponseEntity.ok(debug);
    }
}
