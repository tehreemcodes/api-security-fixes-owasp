/*
 * SECURITY FIX #3 & #6: Excessive Data Exposure & Mass Assignment
 * 
 * VULNERABILITY: API responses returned entire objects including sensitive fields.
 * API inputs accepted any fields, allowing mass assignment attacks.
 * 
 * FIX IMPLEMENTED:
 * - Created Data Transfer Objects (DTOs) to control data exposure
 * - Only include necessary fields in API responses
 * - Validate and whitelist input fields
 * - Separate DTOs for requests and responses
 * - Added @JsonIgnore for sensitive fields
 * 
 * IMPACT: Limits data exposure and prevents unauthorized field modifications
 */

package edu.nu.owaspapivulnlab.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDto {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
    
    @Email
    private String email;
    
    // Note: role and isAdmin are NOT included to prevent mass assignment
}
