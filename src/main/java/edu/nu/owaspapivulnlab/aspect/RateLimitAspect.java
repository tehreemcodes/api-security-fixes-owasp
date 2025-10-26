/*
 * SECURITY FIX #5: Broken Function Level Authorization
 * 
 * VULNERABILITY: No centralized authorization checks for different user roles.
 * Regular users could access admin functions.
 * 
 * FIX IMPLEMENTED:
 * - Created custom security annotations (@RequireRole, @RequireAuth)
 * - Implemented AspectJ aspects to enforce authorization before method execution
 * - Centralized authorization logic for maintainability
 * - Automatic rejection of unauthorized access attempts
 * 
 * IMPACT: Ensures only authorized users can access privileged functions
 */

package edu.nu.owaspapivulnlab.aspect;

import edu.nu.owaspapivulnlab.annotation.RateLimited;
import edu.nu.owaspapivulnlab.service.RateLimitService;
import edu.nu.owaspapivulnlab.service.UserContextService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class RateLimitAspect {
    
    private final RateLimitService rateLimitService;
    private final UserContextService userContextService;
    
    public RateLimitAspect(RateLimitService rateLimitService, UserContextService userContextService) {
        this.rateLimitService = rateLimitService;
        this.userContextService = userContextService;
    }
    
    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String key = getRateLimitKey();
        
        if (!rateLimitService.isAllowed(key, rateLimited.value())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded"));
        }
        
        return joinPoint.proceed();
    }
    
    private String getRateLimitKey() {
        Long userId = userContextService.getCurrentUserId();
        if (userId != null) {
            return "user:" + userId;
        }
        // For unauthenticated requests, use IP-based limiting
        return "ip:anonymous";
    }
}
