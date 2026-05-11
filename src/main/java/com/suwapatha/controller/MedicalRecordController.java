package com.suwapatha.controller;

import com.suwapatha.dto.MedicalRecordResponse;
import com.suwapatha.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final com.suwapatha.repository.UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalRecordResponse>> getMyMedicalRecords(
            Authentication authentication
    ) {
        String patientEmail = authentication.getName();
        com.suwapatha.entity.User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<MedicalRecordResponse> records = medicalRecordService
                .getPatientMedicalRecords(user.getId());
        return ResponseEntity.ok(records);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalRecordResponse>> getFilteredMedicalRecords(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String hospital
    ) {
        String patientEmail = authentication.getName();
        com.suwapatha.entity.User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<MedicalRecordResponse> records = medicalRecordService
                .getFilteredMedicalRecords(user.getId(), startDate, endDate, hospital);
        return ResponseEntity.ok(records);
    }
}