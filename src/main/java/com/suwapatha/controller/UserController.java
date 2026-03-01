package com.suwapatha.controller;

import com.suwapatha.dto.UserResponse;
import com.suwapatha.dto.UpdateProfileRequest;
import com.suwapatha.dto.ChangePasswordRequest;
import com.suwapatha.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateProfile(email, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        userService.changePassword(email, request);
        return ResponseEntity.ok().build();
    }
}