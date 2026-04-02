package com.AgenticAi.AIProject.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import com.AgenticAi.AIProject.DTO.UserResponse;
import com.AgenticAi.AIProject.Entity.User;
import com.AgenticAi.AIProject.Service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081"}, allowCredentials = "true")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/me")
    public UserResponse me() {

        User user = userService.getCurrentUser();

        return new UserResponse(
                user.getEmail(),
                user.getDisplayName(), 
                user.getProfileImage()
        );
    }
}
