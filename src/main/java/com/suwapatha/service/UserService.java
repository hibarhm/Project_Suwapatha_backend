package com.suwapatha.service;

import com.suwapatha.dto.UserResponse;
import com.suwapatha.entity.User;
import com.suwapatha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

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
                .createdAt(user.getCreatedAt())
                .status(user.getStatus())
                .build();
    }
}
