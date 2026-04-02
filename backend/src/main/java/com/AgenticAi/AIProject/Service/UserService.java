package com.AgenticAi.AIProject.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgenticAi.AIProject.Custom.CustomUserDetails;
import com.AgenticAi.AIProject.DTO.LoginRequest;
import com.AgenticAi.AIProject.DTO.RegisterRequest;
import com.AgenticAi.AIProject.Entity.User;
import com.AgenticAi.AIProject.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
private final EmailService emailService;

public void register(RegisterRequest request, MultipartFile profileImage) {

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new RuntimeException("Email already exists");
    }

    String fileName = null;

    if (profileImage != null && !profileImage.isEmpty()) {

        try {
            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            fileName = System.currentTimeMillis() + "_" +
                    profileImage.getOriginalFilename();

            Files.copy(profileImage.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image");
        }
    }

    // 👇 HERE BRO
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setDisplayName(request.getDisplayName());

    if (fileName != null) {
        user.setProfileImage("/uploads/" + fileName);
    }

    userRepository.save(user);

    emailService.sendMail(user);
}


    public void login(LoginRequest request, HttpServletRequest httpRequest) {

        Authentication authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 🔥 Explicitly store SecurityContext in session
        httpRequest.getSession(true)
            .setAttribute("SPRING_SECURITY_CONTEXT",
                SecurityContextHolder.getContext());
    }
    
    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser();
    }



 
}
