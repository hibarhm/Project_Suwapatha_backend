package com.suwapatha.controller;

import com.suwapatha.dto.*;
import com.suwapatha.service.AdminOpdService;
import com.suwapatha.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AdminOpdService adminOpdService;

    public AdminController(UserService userService, AdminOpdService adminOpdService) {
        this.userService = userService;
        this.adminOpdService = adminOpdService;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Hospital Info
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/hospital-info")
    public ResponseEntity<HospitalInfoResponse> getHospitalInfo(Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.getHospitalInfo(adminEmail));
    }

    // ══════════════════════════════════════════════════════════════════════
    // Doctor Management
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/doctors")
    public ResponseEntity<List<UserResponse>> getAllDoctors(Authentication authentication) {
        String adminEmail = authentication.getName();
        String hospitalId = adminOpdService.getAdminHospital(adminEmail).getId();
        return ResponseEntity.ok(userService.getDoctorsByHospital(hospitalId));
    }

    @PutMapping("/doctors/{id}/approve")
    public ResponseEntity<UserResponse> approveDoctor(Authentication authentication, @PathVariable String id) {
        String adminEmail = authentication.getName();
        String hospitalId = adminOpdService.getAdminHospital(adminEmail).getId();

        // Safety check: ensure doctor belongs to the same hospital
        UserResponse doctor = userService.getDoctorsByHospital(hospitalId).stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Doctor not found or does not belong to your hospital"));

        return ResponseEntity.ok(userService.updateUserStatus(id, "APPROVED"));
    }

    @PutMapping("/doctors/{id}/reject")
    public ResponseEntity<UserResponse> rejectDoctor(Authentication authentication, @PathVariable String id) {
        String adminEmail = authentication.getName();
        String hospitalId = adminOpdService.getAdminHospital(adminEmail).getId();

        // Safety check: ensure doctor belongs to the same hospital
        UserResponse doctor = userService.getDoctorsByHospital(hospitalId).stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Doctor not found or does not belong to your hospital"));

        return ResponseEntity.ok(userService.updateUserStatus(id, "REJECTED"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // OPD Session Management - Stats & Overview
    // ══════════════════════════════════════════════════════════════════════

    @GetMapping("/opd/stats/today")
    public ResponseEntity<TodayStatsResponse> getTodayStats(Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.getTodayStats(adminEmail));
    }

    @GetMapping("/opd/sessions/today")
    public ResponseEntity<List<OpdSessionResponse>> getTodaySessions(Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.getTodaySessions(adminEmail));
    }

    @GetMapping("/opd/sessions/upcoming")
    public ResponseEntity<List<OpdSessionResponse>> getUpcomingSessions(Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.getUpcomingSessions(adminEmail));
    }

    @GetMapping("/opd/rooms")
    public ResponseEntity<List<String>> getRooms(Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.getAvailableRooms(adminEmail));
    }

    // ══════════════════════════════════════════════════════════════════════
    // OPD Session Management - CRUD Operations
    // ══════════════════════════════════════════════════════════════════════

    @PostMapping("/sessions")
    public ResponseEntity<OpdSessionResponse> createSession(
            Authentication authentication,
            @Valid @RequestBody CreateOpdSessionRequest request) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.createSession(adminEmail, request));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<OpdSessionResponse>> getAllSessions(Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.getAllSessions(adminEmail));
    }

    @PutMapping("/sessions/{id}")
    public ResponseEntity<OpdSessionResponse> updateSession(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody UpdateOpdSessionRequest request) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminOpdService.updateSession(adminEmail, id, request));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> cancelSession(
            Authentication authentication,
            @PathVariable String id) {
        String adminEmail = authentication.getName();
        adminOpdService.cancelSession(adminEmail, id);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // OPD Session Management - Additional Operations
    // ══════════════════════════════════════════════════════════════════════

    @PutMapping("/sessions/{sessionId}/assign-room")
    public ResponseEntity<OpdSessionResponse> assignRoom(
            Authentication authentication,
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String adminEmail = authentication.getName();
        String room = body.get("room");
        return ResponseEntity.ok(adminOpdService.assignRoom(adminEmail, sessionId, room));
    }

    @PutMapping("/sessions/{sessionId}/assign-doctor")
    public ResponseEntity<OpdSessionResponse> assignDoctor(
            Authentication authentication,
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String adminEmail = authentication.getName();
        String doctorName = body.get("doctorName");
        return ResponseEntity.ok(adminOpdService.assignDoctor(adminEmail, sessionId, doctorName));
    }

    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<OpdSessionResponse> updateSessionStatus(
            Authentication authentication,
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String adminEmail = authentication.getName();
        String status = body.get("status");
        return ResponseEntity.ok(adminOpdService.updateSessionStatus(adminEmail, sessionId, status));
    }
}