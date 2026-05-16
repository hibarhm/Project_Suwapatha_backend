package com.suwapatha.controller;

import com.suwapatha.dto.HospitalResponse;
import com.suwapatha.dto.NearbyHospitalResponse;
import com.suwapatha.dto.OpdSessionResponse;
import com.suwapatha.entity.Hospital;
import com.suwapatha.repository.HospitalRepository;
import com.suwapatha.service.HospitalService;
import com.suwapatha.service.OpdSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalRepository hospitalRepository;
    private final HospitalService hospitalService;
    private final OpdSessionService opdSessionService;

    public HospitalController(HospitalRepository hospitalRepository, 
                            HospitalService hospitalService,
                            OpdSessionService opdSessionService) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalService = hospitalService;
        this.opdSessionService = opdSessionService;
    }

    /* Search hospitals by name (returns all if no query given) */
    @GetMapping
    public ResponseEntity<List<HospitalResponse>> getHospitals(
            @RequestParam(required = false) String search) {

        List<Hospital> hospitals = (search != null && !search.isBlank())
                ? hospitalRepository.findByNameContainingIgnoreCase(search)
                : hospitalRepository.findAll();

        List<HospitalResponse> result = hospitals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /* Get nearby hospitals based on user location */
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyHospitalResponse>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng) {
        return ResponseEntity.ok(hospitalService.getNearbyHospitals(lat, lng));
    }

    /* Get upcoming OPEN OPD sessions for a specific hospital */
    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<OpdSessionResponse>> getSessions(@PathVariable String id) {
        return ResponseEntity.ok(opdSessionService.getUpcomingSessionsForHospital(id));
    }

    private HospitalResponse toResponse(Hospital h) {
        return new HospitalResponse(
                h.getId(), h.getName(), h.getDistrict(),
                h.getProvince(), h.getType(), h.getAddress(), h.getPhone(),
                h.getLatitude(), h.getLongitude());
    }
}