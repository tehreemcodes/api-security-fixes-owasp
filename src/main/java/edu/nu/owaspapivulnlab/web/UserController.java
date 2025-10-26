package edu.nu.owaspapivulnlab.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.service.UserContextService;
import edu.nu.owaspapivulnlab.service.PasswordService;
import edu.nu.owaspapivulnlab.web.dto.UserResponseDto;
import edu.nu.owaspapivulnlab.web.dto.UserCreateDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppUserRepository users;
    private final UserContextService userContextService;
    private final PasswordService passwordService;

    public UserController(AppUserRepository users, UserContextService userContextService, PasswordService passwordService) {
        this.users = users;
        this.userContextService = userContextService;
        this.passwordService = passwordService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        // SECURITY FIX: Enforce ownership with null safety
        Long currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        if (!currentUserId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        
        AppUser user = users.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserCreateDto dto) {
        if (users.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        
        AppUser newUser = AppUser.builder()
                .username(dto.getUsername())
                .password(passwordService.hashPassword(dto.getPassword()))
                .email(dto.getEmail())
                .role("USER")  // Fixed role - no mass assignment
                .isAdmin(false)  // Fixed admin status - no mass assignment
                .build();
        
        AppUser savedUser = users.save(newUser);
        return ResponseEntity.ok(UserResponseDto.from(savedUser));
    }

    // VULNERABILITY(API9: Improper Inventory + API8 Injection style): naive 'search' that can be abused for enumeration
    @GetMapping("/search")
    public List<AppUser> search(@RequestParam String q) {
        return users.search(q);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        // Only admins can list all users
        if (!userContextService.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        
        List<AppUser> allUsers = users.findAll();
        List<UserResponseDto> userDtos = allUsers.stream()
                .map(UserResponseDto::from)
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(userDtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // SECURITY FIX: Enforce ownership with null safety
        Long currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        if (!currentUserId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        
        users.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "deleted");
        return ResponseEntity.ok(response);
    }
}
