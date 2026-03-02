package com.suwapatha.service;

import com.suwapatha.dto.UserResponse;
import com.suwapatha.entity.User;
import com.suwapatha.repository.HospitalRepository;
import com.suwapatha.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final EmailService emailService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, HospitalRepository hospitalRepository,
            EmailService emailService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse updateProfile(String email, com.suwapatha.dto.UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found"));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());

        // Update patient/doctor specific common fields
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPhone(request.getPhoneNumber()); // Sync both phone fields
        }
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());
        if (request.getEmergencyContact() != null)
            user.setEmergencyContact(request.getEmergencyContact());

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public void changePassword(String email, com.suwapatha.dto.ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    public java.util.List<UserResponse> getAllDoctors() {
        return userRepository.findByRole(com.suwapatha.entity.UserRole.DOCTOR).stream()
                .map(this::mapToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<UserResponse> getDoctorsByHospital(String hospitalId) {
        return userRepository.findByRoleAndHospitalId(com.suwapatha.entity.UserRole.DOCTOR, hospitalId).stream()
                .map(this::mapToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public UserResponse updateUserStatus(String id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setStatus(status);
        User savedUser = userRepository.save(user);

        // Send email notification
        String subject = "Account Status Update - Suwapatha";
        String body = "";

        if ("APPROVED".equalsIgnoreCase(status)) {
            body = "Dear Dr. " + user.getLastName() + ",\n\n" +
                    "Your registration with Suwapatha has been APPROVED!\n" +
                    "You can now log in to your dashboard to manage appointments.\n\n" +
                    "Welcome aboard,\nThe Suwapatha Team";
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            body = "Dear Dr. " + user.getLastName() + ",\n\n" +
                    "Your registration with Suwapatha has been REJECTED.\n" +
                    "Please contact support for further information.\n\n" +
                    "Regards,\nThe Suwapatha Team";
        }

        if (!body.isEmpty()) {
            emailService.sendEmail(user.getEmail(), subject, body);
        }

        return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .emergencyContact(user.getEmergencyContact())
                .doctorId(user.getDoctorId())
                .nic(user.getNic())
                .phone(user.getPhone())
                .hospitalId(user.getHospitalId())
                .hospitalName(user.getHospitalId() != null ? hospitalRepository.findById(user.getHospitalId())
                        .map(com.suwapatha.entity.Hospital::getName)
                        .orElse(null) : null)
                .createdAt(user.getCreatedAt())
                .status(user.getStatus())
                .build();
    }
}
