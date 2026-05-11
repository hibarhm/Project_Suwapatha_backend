package com.suwapatha.controller;

import com.suwapatha.dto.*;
import com.suwapatha.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/patient")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody PatientRegisterRequest request) {
        AuthResponse response = authService.registerPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<AuthResponse> registerDoctor(@Valid @RequestBody DoctorRegisterRequest request) {
        AuthResponse response = authService.registerDoctor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login/patient")
    public ResponseEntity<AuthResponse> loginPatient(@Valid @RequestBody PatientLoginRequest request) {
        AuthResponse response = authService.loginPatient(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/doctor")
    public ResponseEntity<AuthResponse> loginDoctor(@Valid @RequestBody DoctorLoginRequest request) {
        AuthResponse response = authService.loginDoctor(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/admin")
    public ResponseEntity<AuthResponse> loginAdmin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/super-admin")
    public ResponseEntity<AuthResponse> loginSuperAdmin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginSuperAdmin(request);
        return ResponseEntity.ok(response);
    }

}
