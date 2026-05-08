package com.suwapatha.controller;

import com.suwapatha.dto.CreateHospitalAdminRequest;
import com.suwapatha.dto.SuperAdminHospitalResponse;
import com.suwapatha.dto.UpdateHospitalAdminRequest;
import com.suwapatha.dto.UserResponse;
import com.suwapatha.service.SuperAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    @GetMapping("/hospitals")
    public ResponseEntity<List<SuperAdminHospitalResponse>> getHospitals() {
        return ResponseEntity.ok(superAdminService.getHospitalsWithAdmin());
    }

    @PostMapping("/hospitals/{hospitalId}/admins")
    public ResponseEntity<UserResponse> createHospitalAdmin(
            @PathVariable String hospitalId,
            @Valid @RequestBody CreateHospitalAdminRequest request) {
        UserResponse created = superAdminService.createHospitalAdmin(hospitalId, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/hospitals/{hospitalId}/admins")
    public ResponseEntity<UserResponse> updateHospitalAdmin(
            @PathVariable String hospitalId,
            @Valid @RequestBody UpdateHospitalAdminRequest request) {
        return ResponseEntity.ok(superAdminService.updateHospitalAdmin(hospitalId, request));
    }
}


