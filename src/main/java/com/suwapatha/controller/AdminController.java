package com.suwapatha.controller;

import com.suwapatha.dto.*;
import com.suwapatha.service.OpdSessionService;
import com.suwapatha.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OpdSessionService opdSessionService;

    // ── Doctor Management ──────────────────────────────────────────────────

    @GetMapping("/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllDoctors() {
        return ResponseEntity.ok(userService.getAllDoctors());
    }

    @PutMapping("/doctors/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> approveDoctor(@PathVariable String id) {
        return ResponseEntity.ok(userService.updateUserStatus(id, "APPROVED"));
    }

    @PutMapping("/doctors/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> rejectDoctor(@PathVariable String id) {
        return ResponseEntity.ok(userService.updateUserStatus(id, "REJECTED"));
    }

    // ── OPD Session Management ─────────────────────────────────────────────

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OpdSessionResponse> createSession(
            @Valid @RequestBody CreateOpdSessionRequest request) {
        return ResponseEntity.ok(opdSessionService.createSession(request));
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OpdSessionResponse>> getAllSessions() {
        return ResponseEntity.ok(opdSessionService.getAllSessions());
    }

    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OpdSessionResponse> updateSession(
            @PathVariable String id,
            @RequestBody UpdateOpdSessionRequest request) {
        return ResponseEntity.ok(opdSessionService.updateSession(id, request));
    }

    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelSession(@PathVariable String id) {
        opdSessionService.cancelSession(id);
        return ResponseEntity.noContent().build();
    }
}
