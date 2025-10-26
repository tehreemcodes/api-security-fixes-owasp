/*
 * SECURITY FIX #4: Lack of Resources & Rate Limiting
 * 
 * VULNERABILITY: The original application had no rate limiting, allowing unlimited
 * API requests which could lead to DoS attacks and resource exhaustion.
 * 
 * FIX IMPLEMENTED:
 * - Added RateLimitService to track and limit API requests
 * - Implemented token bucket algorithm for rate limiting
 * - Different rate limits for different API types (login, general API)
 * - Configured limits: 5 login attempts per minute, 100 general requests per minute
 * - Returns 429 (Too Many Requests) when limit exceeded
 * 
 * IMPACT: Prevents denial of service attacks and ensures fair resource usage
 */

package edu.nu.owaspapivulnlab.annotation;

import edu.nu.owaspapivulnlab.service.RateLimitService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    RateLimitService.RateLimitType value() default RateLimitService.RateLimitType.GENERAL;
}
