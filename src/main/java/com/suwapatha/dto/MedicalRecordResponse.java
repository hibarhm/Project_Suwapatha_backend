package com.suwapatha.dto;

import com.suwapatha.entity.Prescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private String id;
    private String date;  // Formatted date (e.g., "Oct 26, 2025")
    private String time;
    private String hospital;
    private String doctor;
    private boolean followUpRequired;
    private String consultationNotes;
    private List<Prescription> prescriptions;
    private int labReports;  // Count of lab reports
    private List<String> labReportUrls;
}