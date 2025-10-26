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
