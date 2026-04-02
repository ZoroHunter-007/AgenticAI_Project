package com.AgenticAi.AIProject.Configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.AgenticAi.AIProject.Security.CustomOidcUserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


	private final CustomOidcUserService customOidcUserService;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
        .csrf(csrf -> csrf.disable())

            // ✅ Session-based authentication
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .authorizeHttpRequests(auth -> auth

            	    // Public static
            	    .requestMatchers(
            	        "/",
            	        "/index.html",
            	        "/style.css",
            	        "/script.js",
            	        "/Account.html",
            	        "/register.js",
            	        "/login.js",
            	        "/auth.css",
            	        "/uploads/**"
            	    ).permitAll()

            	    // OAuth endpoints must be public
            	    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

            	    // Only these auth endpoints are public
            	    .requestMatchers(
            	        "/api/auth/register",
            	        "/api/auth/login",
            	        "/api/auth/logout"
            	    ).permitAll()

            	    // Everything else requires authentication
            	    .anyRequest().authenticated()
            	)

            .formLogin(form -> form.disable())
            .oauth2Login(oauth -> oauth
            	    .userInfoEndpoint(user ->
            	        user.oidcUserService(customOidcUserService)
            	    )
            	    // Change this line ↓
            	    .defaultSuccessUrl("http://localhost:5173/chat", true)
            	)
            	.logout(logout -> logout
            	    .logoutUrl("/api/auth/logout")
            	    .logoutSuccessUrl("http://localhost:5173/login") // fix this too
            	);

        return http.build();
    }
   

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @PostConstruct
    public void checkEnv() {
        System.out.println("Resolved Client ID: " + clientId);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow your Vite frontend
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8081")); 
        // Allow standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // Crucial for sessions/cookies to work
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all API endpoints
        return source;
    }
}
