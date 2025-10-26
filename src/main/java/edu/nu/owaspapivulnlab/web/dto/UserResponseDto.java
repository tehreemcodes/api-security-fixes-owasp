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
