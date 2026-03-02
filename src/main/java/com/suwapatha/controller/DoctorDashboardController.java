package com.suwapatha.controller;

import com.suwapatha.dto.ConsultationRequest;
import com.suwapatha.dto.DoctorDashboardResponse;
import com.suwapatha.dto.DoctorPatientResponse;
import com.suwapatha.dto.PatientDetailsResponse;
import com.suwapatha.service.DoctorDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@Slf4j
public class DoctorDashboardController {

    private final DoctorDashboardService doctorDashboardService;

    @GetMapping("/dashboard")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorDashboardResponse> getDashboard(Authentication authentication) {
        if (authentication == null) {
            log.warn("GET /api/doctor/dashboard - No authentication found");
            return ResponseEntity.status(401).build();
        }

        log.info("GET /api/doctor/dashboard - User: {}, Authorities: {}",
                authentication.getName(), authentication.getAuthorities());

        String doctorEmail = authentication.getName();
        return ResponseEntity.ok(doctorDashboardService.getDashboardData(doctorEmail));
    }

    @GetMapping("/patients")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorPatientResponse>> getPatients(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String doctorEmail = authentication.getName();
        return ResponseEntity.ok(doctorDashboardService.getDoctorPatients(doctorEmail));
    }

    @GetMapping("/patients/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientDetailsResponse> getPatientDetails(
            @org.springframework.web.bind.annotation.PathVariable String id) {
        return ResponseEntity.ok(doctorDashboardService.getPatientDetails(id));
    }

    @org.springframework.web.bind.annotation.PostMapping("/consultation")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> saveConsultation(
            @org.springframework.web.bind.annotation.RequestBody ConsultationRequest request,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String doctorEmail = authentication.getName();
        doctorDashboardService.saveConsultation(request, doctorEmail);
        return ResponseEntity.ok("Consultation saved successfully");
    }
}
