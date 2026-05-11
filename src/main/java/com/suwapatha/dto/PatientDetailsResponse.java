package com.suwapatha.dto;

import com.suwapatha.entity.MedicalRecord;
import com.suwapatha.entity.Prescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDetailsResponse {
    private String id;
    private String name;
    private String age;
    private String gender;
    private String bloodType;
    private String phone;
    private String email;
    private String address;
    private String emergencyContact;
    private List<String> allergies;
    private List<String> chronicConditions;

    private List<MedicalRecord> medicalHistory;
    private List<Prescription> activePrescriptions;

    private String currentAppointmentId;
    private String currentStatus;
    private String hospitalName;
}
