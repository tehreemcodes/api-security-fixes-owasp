package edu.nu.owaspapivulnlab.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import edu.nu.owaspapivulnlab.model.Account;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AccountRepository;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.service.PasswordService;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(AppUserRepository users, AccountRepository accounts, PasswordService passwordService) {
        return args -> {
            try {
                System.out.println("DataSeeder: Starting seed process...");
                System.out.println("DataSeeder: Current user count: " + users.count());
                
                if (users.count() == 0) {
                    System.out.println("DataSeeder: Creating seed users...");
                    
                    // Create Alice
                    AppUser u1 = users.save(AppUser.builder()
                            .username("alice")
                            .password(passwordService.hashPassword("alice123"))
                            .email("alice@cydea.tech")
                            .role("USER")
                            .isAdmin(false)
                            .build());
                    System.out.println("DataSeeder: Created Alice with ID: " + u1.getId());
                    
                    // Create Bob
                    AppUser u2 = users.save(AppUser.builder()
                            .username("bob")
                            .password(passwordService.hashPassword("bob123"))
                            .email("bob@cydea.tech")
                            .role("ADMIN")
                            .isAdmin(true)
                            .build());
                    System.out.println("DataSeeder: Created Bob with ID: " + u2.getId());
                    
                    // Create accounts
                    accounts.save(Account.builder().ownerUserId(u1.getId()).iban("PK00-ALICE").balance(1000.0).build());
                    accounts.save(Account.builder().ownerUserId(u2.getId()).iban("PK00-BOB").balance(5000.0).build());
                    System.out.println("DataSeeder: Created accounts for Alice and Bob");
                } else {
                    System.out.println("DataSeeder: Users already exist, skipping seed");
                }
                
                System.out.println("DataSeeder: Seed process completed");
            } catch (Exception e) {
                System.err.println("DataSeeder: Error during seeding: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
