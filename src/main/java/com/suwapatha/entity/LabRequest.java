package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lab_requests")
public class LabRequest {

    @Id
    private String id;

    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String hospitalId;

    private List<String> requestedTests = new ArrayList<>();
    private String notes;
    private Priority priority = Priority.NORMAL;
    private Status status = Status.PENDING;

    private List<LabTestResult> results = new ArrayList<>();
    private List<String> reportUrls = new ArrayList<>();

    private String completedBy; // Lab Staff ID
    private LocalDateTime completedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Priority {
        NORMAL, URGENT
    }

    public enum Status {
        PENDING, SAMPLE_COLLECTED, PROCESSING, COMPLETED
    }
}
