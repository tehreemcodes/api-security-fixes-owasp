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

import edu.nu.owaspapivulnlab.model.AppUser;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    
    public static UserResponseDto from(AppUser user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
