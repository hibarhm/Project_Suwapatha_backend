package com.suwapatha.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;
}
