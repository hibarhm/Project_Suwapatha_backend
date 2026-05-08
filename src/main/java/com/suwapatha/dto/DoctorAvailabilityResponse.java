package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityResponse {
    private String id;
    private String doctorId;
    private String doctorName;
    private String email;
    private String date;
    private boolean available;
    private String note;
    private String room;
    private String updatedAt; // ISO string for easy frontend consumption
}
