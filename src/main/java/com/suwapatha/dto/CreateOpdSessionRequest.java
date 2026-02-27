package com.suwapatha.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOpdSessionRequest {

    @NotBlank(message = "Hospital ID is required")
    private String hospitalId;

    @NotBlank(message = "Date is required")
    private String date; // yyyy-MM-dd

    @NotBlank(message = "Start time is required")
    private String startTime; // HH:mm

    @NotBlank(message = "End time is required")
    private String endTime; // HH:mm

    @NotBlank(message = "Department is required")
    private String department;

    private String doctorName;
    private String room;

    @Min(value = 1, message = "Max queue size must be at least 1")
    private int maxQueueSize;
}
