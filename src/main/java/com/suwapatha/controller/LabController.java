package com.suwapatha.controller;

import com.suwapatha.entity.LabRequest;
import com.suwapatha.entity.LabTestResult;
import com.suwapatha.service.LabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/laboratory")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LabRequest> createRequest(@RequestBody LabRequest labRequest) {
        return ResponseEntity.ok(labService.createLabRequest(labRequest));
    }

    @GetMapping("/request/{id}")
    @PreAuthorize("hasAnyRole('LABORATORY', 'DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<LabRequest> getRequest(@PathVariable String id) {
        return ResponseEntity.ok(labService.getRequestById(id));
    }

    @GetMapping("/hospital/{hospitalId}")
    @PreAuthorize("hasAnyRole('LABORATORY', 'ADMIN')")
    public ResponseEntity<List<LabRequest>> getHospitalRequests(@PathVariable String hospitalId) {
        return ResponseEntity.ok(labService.getAllRequestsByHospital(hospitalId));
    }

    @GetMapping("/hospital/{hospitalId}/status/{status}")
    @PreAuthorize("hasAnyRole('LABORATORY', 'ADMIN')")
    public ResponseEntity<List<LabRequest>> getHospitalRequestsByStatus(
            @PathVariable String hospitalId,
            @PathVariable LabRequest.Status status) {
        return ResponseEntity.ok(labService.getRequestsByHospitalAndStatus(hospitalId, status));
    }

    @PutMapping("/request/{id}/status")
    @PreAuthorize("hasRole('LABORATORY')")
    public ResponseEntity<LabRequest> updateStatus(
            @PathVariable String id,
            @RequestParam LabRequest.Status status) {
        return ResponseEntity.ok(labService.updateStatus(id, status));
    }

    @PutMapping("/request/{id}/results")
    @PreAuthorize("hasRole('LABORATORY')")
    public ResponseEntity<LabRequest> submitResults(
            @PathVariable String id,
            @RequestBody ResultSubmitRequest resultRequest) {
        return ResponseEntity.ok(labService.submitResults(
                id,
                resultRequest.getResults(),
                resultRequest.getReportUrls(),
                resultRequest.getStaffId()));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'LABORATORY')")
    public ResponseEntity<List<LabRequest>> getPatientHistory(@PathVariable String patientId) {
        return ResponseEntity.ok(labService.getPatientLabHistory(patientId));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('LABORATORY')")
    public ResponseEntity<String> uploadReport(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        // For demo purposes, we'll save to a local folder 'uploads'
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/reports");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Return the URL (in a real app, this would be a cloud storage URL)
        return ResponseEntity.ok("/api/laboratory/reports/" + fileName);
    }

    // Static inner class for result submission request
    @lombok.Data
    public static class ResultSubmitRequest {
        private List<LabTestResult> results;
        private List<String> reportUrls;
        private String staffId;
    }
}
