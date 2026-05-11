package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private UserRole role;

    // Patient-specific fields
    private String dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String address;
    private String emergencyContact;
    private String bloodType;

    // Doctor-specific fields
    @Indexed(unique = true, sparse = true)
    private String doctorId;
    private String nic;
    private String phone;
    private String hospitalId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private boolean enabled = true;

    private String status; // PENDING, APPROVED, REJECTED

    private int lateCancellationCount = 0;
    private boolean hasRedMark = false;
}
