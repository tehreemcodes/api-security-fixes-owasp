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
