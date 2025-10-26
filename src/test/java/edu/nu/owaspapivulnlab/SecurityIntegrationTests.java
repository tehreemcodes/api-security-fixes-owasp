package edu.nu.owaspapivulnlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.nu.owaspapivulnlab.model.Account;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AccountRepository;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class SecurityIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordService passwordService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Clean up test data
        accountRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test users with hashed passwords
        AppUser alice = AppUser.builder()
                .username("alice")
                .password(passwordService.hashPassword("alice123"))
                .email("alice@test.com")
                .role("USER")
                .isAdmin(false)
                .build();
        userRepository.save(alice);
        
        AppUser bob = AppUser.builder()
                .username("bob")
                .password(passwordService.hashPassword("bob123"))
                .email("bob@test.com")
                .role("ADMIN")
                .isAdmin(true)
                .build();
        userRepository.save(bob);
        
        // Create test accounts
        accountRepository.save(Account.builder()
                .ownerUserId(alice.getId())
                .iban("PK00-ALICE")
                .balance(1000.0)
                .build());
        
        accountRepository.save(Account.builder()
                .ownerUserId(bob.getId())
                .iban("PK00-BOB")
                .balance(5000.0)
                .build());
    }

    @Test
    void testAuthenticationRequired() throws Exception {
        mockMvc.perform(get("/api/accounts/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testValidLogin() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "alice");
        loginRequest.put("password", "alice123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testInvalidLogin() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "alice");
        loginRequest.put("password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUserCanOnlyAccessOwnAccount() throws Exception {
        // Login as alice
        String token = getToken("alice", "alice123");
        
        // Alice should be able to access her own account
        mockMvc.perform(get("/api/accounts/mine")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        
        // Alice should not be able to access bob's account balance
        mockMvc.perform(get("/api/accounts/2/balance")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testMassAssignmentPrevention() throws Exception {
        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put("username", "newuser");
        userRequest.put("password", "password123");
        userRequest.put("email", "newuser@test.com");
        userRequest.put("role", "ADMIN");  // This should be ignored
        userRequest.put("isAdmin", true);  // This should be ignored

        String token = getToken("alice", "alice123");
        
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.isAdmin").doesNotExist());
    }

    @Test
    void testInputValidation() throws Exception {
        String token = getToken("alice", "alice123");
        
        // Test negative amount
        mockMvc.perform(post("/api/accounts/1/transfer?amount=-100")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        
        // Test zero amount
        mockMvc.perform(post("/api/accounts/1/transfer?amount=0")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRateLimiting() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "alice");
        loginRequest.put("password", "wrongpassword");

        // Make multiple failed login attempts
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().is(i < 5 ? 401 : 429)); // Should be rate limited after 5 attempts
        }
    }

    @Test
    void testJwtValidation() throws Exception {
        // Test with invalid token
        mockMvc.perform(get("/api/accounts/mine")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminOnlyEndpoints() throws Exception {
        String userToken = getToken("alice", "alice123");
        String adminToken = getToken("bob", "bob123");
        
        // Regular user should not access admin endpoints
        mockMvc.perform(get("/api/admin/metrics")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        
        // Admin should access admin endpoints
        mockMvc.perform(get("/api/admin/metrics")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private String getToken(String username, String password) throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> responseMap = objectMapper.readValue(response, Map.class);
        return responseMap.get("token");
    }
}
