package edu.nu.owaspapivulnlab.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer:owasp-api-lab}")
    private String issuer;

    @Value("${app.jwt.audience:api-users}")
    private String audience;

    // VULNERABILITY(API7 Security Misconfiguration): overly permissive CORS/CSRF and antMatchers order
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()); // APIs typically stateless; but add CSRF for state-changing in real apps
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(reg -> reg
                .requestMatchers("/api/auth/login", "/api/auth/signup", "/h2-console/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
        );

        http.headers(h -> h.frameOptions(f -> f.disable())); // allow H2 console

        http.addFilterBefore(new JwtFilter(secret, issuer, audience), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Improved JWT filter with proper error handling
    static class JwtFilter extends OncePerRequestFilter {
        private final String secret;
        private final String issuer;
        private final String audience;
        
        JwtFilter(String secret, String issuer, String audience) { 
            this.secret = secret; 
            this.issuer = issuer;
            this.audience = audience;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    Claims c = Jwts.parserBuilder()
                            .setSigningKey(secret.getBytes())
                            .requireIssuer(issuer)
                            .requireAudience(audience)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                    
                    // Validate token is not expired
                    if (c.getExpiration().before(new Date())) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    
                    String user = c.getSubject();
                    String role = (String) c.get("role");
                    UsernamePasswordAuthenticationToken authn = new UsernamePasswordAuthenticationToken(user, null,
                            role != null ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) : Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authn);
                } catch (JwtException | IllegalArgumentException e) {
                    // Properly handle JWT errors - return 401
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }
}
