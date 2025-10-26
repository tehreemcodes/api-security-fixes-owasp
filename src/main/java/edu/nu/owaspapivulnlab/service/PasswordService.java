/*
 * SECURITY FIX #2: Broken Authentication
 * 
 * VULNERABILITY: Passwords were stored in plain text or with weak hashing,
 * making them vulnerable if the database was compromised.
 * 
 * FIX IMPLEMENTED:
 * - Added PasswordService using BCrypt for secure password hashing
 * - Implemented password strength validation (minimum length, complexity)
 * - Uses BCrypt with salt for one-way hashing (cannot be reversed)
 * - Password verification without exposing the hash
 * 
 * IMPACT: Protects user credentials even if database is compromised
 */

package edu.nu.owaspapivulnlab.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * SECURITY FIX: API2:2023 - Broken Authentication
 * 
 * This service implements secure password hashing using BCrypt to address
 * the broken authentication vulnerability. BCrypt provides:
 * - Salted password hashing to prevent rainbow table attacks
 * - Adaptive hashing with configurable cost factor
 * - Protection against timing attacks
 * 
 * OWASP Recommendation: Use strong, salted password hashing algorithms
 * 
 * @author Security Team
 * @version 1.0
 * @since 2024-12
 */
@Service
public class PasswordService {
    
    /**
     * BCrypt password encoder with default strength (10 rounds)
     * This provides a good balance between security and performance
     * Each round doubles the computational cost, making brute force attacks impractical
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * SECURITY FIX: Hash plain text passwords using BCrypt
     * 
     * This method replaces plain text password storage with secure hashing.
     * BCrypt automatically generates a unique salt for each password,
     * preventing rainbow table attacks and ensuring password uniqueness.
     * 
     * @param plainPassword The plain text password to hash
     * @return BCrypt hashed password with embedded salt
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * SECURITY FIX: Verify password against stored hash
     * 
     * This method securely compares a plain text password with a stored hash.
     * BCrypt handles salt extraction and comparison internally, preventing
     * timing attacks and ensuring constant-time comparison.
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The stored BCrypt hash
     * @return true if password matches, false otherwise
     */
    public boolean matches(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
