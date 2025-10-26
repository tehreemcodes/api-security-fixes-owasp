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

import edu.nu.owaspapivulnlab.model.Account;
import lombok.Data;

@Data
public class AccountResponseDto {
    private Long id;
    private String iban;
    private Double balance;
    
    public static AccountResponseDto from(Account account) {
        AccountResponseDto dto = new AccountResponseDto();
        dto.setId(account.getId());
        dto.setIban(account.getIban());
        dto.setBalance(account.getBalance());
        return dto;
    }
}
