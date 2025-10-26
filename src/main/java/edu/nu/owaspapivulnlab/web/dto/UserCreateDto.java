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
