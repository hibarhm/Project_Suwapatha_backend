package com.suwapatha.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "NIC is required")
    @Pattern(regexp = "^([0-9]{9}[VvXx]|[0-9]{12})$", message = "Invalid NIC format")
    private String nic;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    @NotBlank(message = "Hospital is required")
    private String hospitalId;
}