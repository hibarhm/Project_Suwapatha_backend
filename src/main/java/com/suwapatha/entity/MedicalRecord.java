package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medical_records")
public class MedicalRecord {

    @Id
    private String id;

    private String patientId;
    private String patientName;

    private LocalDateTime visitDate;
    private String visitTime;

    private String hospital;
    private String doctorId;
    private String doctorName;

    private boolean followUpRequired;
    private String diagnosis;
    private String consultationNotes;

    // Vitals
    private String bp;
    private String temp;
    private String pulse;
    private String weight;

    private List<Prescription> prescriptions;
    private List<String> labReportUrls; // URLs to stored lab reports

    @CreatedDate
    private LocalDateTime createdAt;
}