package com.suwapatha.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLoginRequest {

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "Password is required")
    private String password;
}
