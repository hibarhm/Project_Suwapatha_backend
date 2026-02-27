package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordFilterRequest {
    private String startDate;  // Optional
    private String endDate;    // Optional
    private String hospital;   // Optional
    private String visitType;  // Optional (routine, followup, emergency)
}