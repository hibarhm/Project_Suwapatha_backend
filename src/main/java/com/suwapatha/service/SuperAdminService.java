package com.suwapatha.service;

import com.suwapatha.dto.CreateHospitalAdminRequest;
import com.suwapatha.dto.SuperAdminHospitalAdminResponse;
import com.suwapatha.dto.SuperAdminHospitalResponse;
import com.suwapatha.dto.UpdateHospitalAdminRequest;
import com.suwapatha.dto.UserResponse;
import com.suwapatha.entity.Hospital;
import com.suwapatha.entity.User;
import com.suwapatha.entity.UserRole;
import com.suwapatha.exception.ResourceNotFoundException;
import com.suwapatha.exception.UserAlreadyExistsException;
import com.suwapatha.repository.HospitalRepository;
import com.suwapatha.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuperAdminService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminService(
            UserRepository userRepository,
            HospitalRepository hospitalRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createHospitalAdmin(String hospitalId, CreateHospitalAdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", hospitalId));

        if (hospital.getAdminId() != null && !hospital.getAdminId().isBlank()) {
            throw new IllegalArgumentException("Hospital already has an assigned admin");
        }

        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setRole(UserRole.ADMIN);
        admin.setHospitalId(hospitalId);
        admin.setEnabled(true);
        admin.setStatus("APPROVED");

        User savedAdmin = userRepository.save(admin);

        hospital.setAdminId(savedAdmin.getId());
        hospitalRepository.save(hospital);

        return UserResponse.builder()
                .id(savedAdmin.getId())
                .email(savedAdmin.getEmail())
                .firstName(savedAdmin.getFirstName())
                .lastName(savedAdmin.getLastName())
                .role(savedAdmin.getRole())
                .hospitalId(savedAdmin.getHospitalId())
                .hospitalName(hospital.getName())
                .status(savedAdmin.getStatus())
                .createdAt(savedAdmin.getCreatedAt())
                .build();
    }

    public List<SuperAdminHospitalResponse> getHospitalsWithAdmin() {
        return hospitalRepository.findAll().stream()
                .map(h -> {
                    SuperAdminHospitalAdminResponse admin = null;
                    boolean hasAdmin = h.getAdminId() != null && !h.getAdminId().isBlank();
                    if (hasAdmin) {
                        admin = userRepository.findById(h.getAdminId())
                                .filter(u -> u.getRole() == UserRole.ADMIN)
                                .map(u -> new SuperAdminHospitalAdminResponse(
                                        u.getId(),
                                        u.getEmail(),
                                        u.getFirstName(),
                                        u.getLastName()))
                                .orElse(null);
                    }
                    return new SuperAdminHospitalResponse(
                            h.getId(),
                            h.getName(),
                            h.getDistrict(),
                            h.getProvince(),
                            h.getType(),
                            h.getAddress(),
                            h.getPhone(),
                            admin != null,
                            admin);
                })
                .toList();
    }

    public UserResponse updateHospitalAdmin(String hospitalId, UpdateHospitalAdminRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", hospitalId));

        if (hospital.getAdminId() == null || hospital.getAdminId().isBlank()) {
            throw new IllegalArgumentException("Hospital does not have an assigned admin");
        }

        User admin = userRepository.findById(hospital.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", hospital.getAdminId()));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Assigned user is not an ADMIN");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!admin.getEmail().equalsIgnoreCase(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("User with email " + normalizedEmail + " already exists");
        }

        admin.setEmail(normalizedEmail);
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(admin);
        return UserResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .role(saved.getRole())
                .hospitalId(saved.getHospitalId())
                .hospitalName(hospital.getName())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}

