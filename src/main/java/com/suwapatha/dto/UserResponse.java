package com.suwapatha.dto;

import com.suwapatha.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String email;

    private String firstName;

    private String lastName;

    private UserRole role;

    // Patient-specific fields
    private String dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String address;
    private String emergencyContact;

    // Doctor-specific fields
    private String doctorId;
    private String nic;
    private String phone;
    private String hospitalId;
    private String hospitalName;

    private LocalDateTime createdAt;

    private String status;
    private int lateCancellationCount;
    private boolean hasRedMark;
    private boolean enabled;
}
