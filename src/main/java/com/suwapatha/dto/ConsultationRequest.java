package com.suwapatha.dto;

import com.suwapatha.entity.Prescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {
    private String patientId;
    private String diagnosis;
    private String consultationNotes;

    // Vitals
    private String bp;
    private String temp;
    private String pulse;
    private String weight;

    private List<Prescription> prescriptions;
    private boolean followUpRequired;
    private String hospitalName;
    private String appointmentId;
}
