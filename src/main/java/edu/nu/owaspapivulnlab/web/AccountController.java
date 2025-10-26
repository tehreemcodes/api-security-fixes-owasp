package edu.nu.owaspapivulnlab.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.model.Account;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AccountRepository;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.service.UserContextService;
import edu.nu.owaspapivulnlab.web.dto.AccountResponseDto;
import edu.nu.owaspapivulnlab.annotation.RateLimited;
import edu.nu.owaspapivulnlab.service.RateLimitService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accounts;
    private final AppUserRepository users;
    private final UserContextService userContextService;

    public AccountController(AccountRepository accounts, AppUserRepository users, UserContextService userContextService) {
        this.accounts = accounts;
        this.users = users;
        this.userContextService = userContextService;
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<?> balance(@PathVariable Long id) {
        Account a = accounts.findById(id).orElse(null);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }
        
        // SECURITY FIX: Enforce ownership with null safety
        Long currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        if (!a.getOwnerUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        
        return ResponseEntity.ok(a.getBalance());
    }

    @RateLimited(RateLimitService.RateLimitType.TRANSFER)
    @PostMapping("/{id}/transfer")
    public ResponseEntity<?> transfer(@PathVariable Long id, @RequestParam Double amount) {
        Account a = accounts.findById(id).orElse(null);
        if (a == null) {
            return ResponseEntity.notFound().build();
        }
        
        // SECURITY FIX: Enforce ownership with null safety
        Long currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        if (!a.getOwnerUserId().equals(currentUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        
        // Input validation: reject negative or zero amounts
        if (amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Amount must be positive"));
        }
        
        // Check sufficient balance
        if (a.getBalance() < amount) {
            return ResponseEntity.badRequest().body(Map.of("error", "Insufficient balance"));
        }
        
        a.setBalance(a.getBalance() - amount);
        accounts.save(a);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("remaining", a.getBalance());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine() {
        Long currentUserId = userContextService.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        List<Account> userAccounts = accounts.findByOwnerUserId(currentUserId);
        List<AccountResponseDto> accountDtos = userAccounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(accountDtos);
    }
}
