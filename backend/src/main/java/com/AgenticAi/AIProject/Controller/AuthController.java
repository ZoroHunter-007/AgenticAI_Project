package com.AgenticAi.AIProject.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.AgenticAi.AIProject.Custom.CustomUserDetails;
import com.AgenticAi.AIProject.DTO.LoginRequest;
import com.AgenticAi.AIProject.DTO.RegisterRequest;
import com.AgenticAi.AIProject.Entity.User;
import com.AgenticAi.AIProject.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081"}, allowCredentials = "true")
public class AuthController {

    private final UserService authService;
    @PostMapping(
            value = "/register",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> register(
            @ModelAttribute RegisterRequest request,
            @RequestParam("profileImage") MultipartFile multipartFile
    ) {

        System.out.println("REGISTER CONTROLLER HIT");
        System.out.println("Email: " + request.getEmail());
        System.out.println("DisplayName: " + request.getDisplayName());

        authService.register(request, multipartFile);

        return ResponseEntity.ok(Map.of("message","Verification Mail Sent!"));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        authService.login(request, httpRequest);

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
    	request.getSession().invalidate();
    	return ResponseEntity.ok(Map.of("message", "Logout Successfully"));
    }
    @GetMapping("/me")
    public User me(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return null;
        }

        return userDetails.getUser();
    }
    
}
