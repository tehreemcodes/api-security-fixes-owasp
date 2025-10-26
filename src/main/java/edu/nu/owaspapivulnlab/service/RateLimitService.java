package edu.nu.owaspapivulnlab.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SECURITY FIX: API4:2023 - Unrestricted Resource Consumption
 * 
 * This service implements rate limiting to prevent abuse and DoS attacks.
 * It uses the token bucket algorithm to control request rates for different
 * types of operations, protecting against:
 * - Brute force attacks on login endpoints
 * - DoS attacks on transfer operations
 * - General API abuse
 * 
 * Key Security Features:
 * - IP-based rate limiting
 * - Endpoint-specific limits
 * - Configurable rate thresholds
 * - Automatic rate limit recovery
 * 
 * OWASP Recommendation: Implement rate limiting on all API endpoints
 * 
 * @author Security Team
 * @version 1.0
 * @since 2024-12
 */
@Service
public class RateLimitService {
    
    /**
     * Thread-safe map to store rate limit buckets per IP address
     * Each IP gets its own bucket to prevent cross-contamination
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // SECURITY FIX: Conservative rate limits to prevent abuse
    private static final int LOGIN_ATTEMPTS_PER_MINUTE = 5;      // Prevent brute force
    private static final int TRANSFER_ATTEMPTS_PER_MINUTE = 10;   // Prevent financial abuse
    private static final int GENERAL_REQUESTS_PER_MINUTE = 100;   // Prevent DoS
    
    /**
     * SECURITY FIX: Check if request is allowed based on rate limits
     * 
     * This method implements the core rate limiting logic using the token
     * bucket algorithm. Each IP address gets its own bucket to prevent
     * one user's requests from affecting another user's rate limits.
     * 
     * @param key Unique identifier (typically IP address)
     * @param type Type of operation being rate limited
     * @return true if request is allowed, false if rate limited
     */
    public boolean isAllowed(String key, RateLimitType type) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(type));
        return bucket.tryConsume(1);
    }
    
    /**
     * SECURITY FIX: Create rate limit bucket for specific operation type
     * 
     * This method creates appropriate rate limit buckets based on the
     * operation type. Different operations have different risk profiles
     * and therefore different rate limits.
     * 
     * @param type The type of operation being rate limited
     * @return Configured rate limit bucket
     */
    private Bucket createBucket(RateLimitType type) {
        Bandwidth limit;
        
        switch (type) {
            case LOGIN:
                // SECURITY FIX: Strict limits on login to prevent brute force
                limit = Bandwidth.classic(LOGIN_ATTEMPTS_PER_MINUTE, 
                    Refill.intervally(LOGIN_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1)));
                break;
            case TRANSFER:
                // SECURITY FIX: Moderate limits on transfers to prevent abuse
                limit = Bandwidth.classic(TRANSFER_ATTEMPTS_PER_MINUTE, 
                    Refill.intervally(TRANSFER_ATTEMPTS_PER_MINUTE, Duration.ofMinutes(1)));
                break;
            case GENERAL:
            default:
                // SECURITY FIX: Generous limits for general API usage
                limit = Bandwidth.classic(GENERAL_REQUESTS_PER_MINUTE, 
                    Refill.intervally(GENERAL_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
                break;
        }
        
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * SECURITY FIX: Rate limit types for different operation categories
     * 
     * This enum defines the different types of operations that need
     * rate limiting, each with appropriate security thresholds.
     */
    public enum RateLimitType {
        LOGIN,      // Authentication operations - strict limits
        TRANSFER,   // Financial operations - moderate limits
        GENERAL     // General API operations - generous limits
    }
}
