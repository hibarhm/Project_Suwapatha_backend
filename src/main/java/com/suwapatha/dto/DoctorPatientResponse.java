package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPatientResponse {
    private String id;
    private String queueNo;
    private String name;
    private String gender;
    private String time;
    private String status;
    private String patientId; // Used for navigation
}
