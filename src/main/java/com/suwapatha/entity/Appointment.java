package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointments")
public class Appointment {

    @Id
    private String id;

    private String patientId;
    private String patientEmail;

    private String sessionId; // links to OpdSession
    private String hospitalName; // denormalised for display
    private String appointmentDate; // yyyy-MM-dd

    private int queueNumber;

    // Derived from the session
    private String doctorId;
    private String doctorEmail;
    private String doctorName;
    private String room;

    // BOOKED | CANCELLED | COMPLETED | PENDING_ALLOCATION
    private String status;

    private int estimatedWaitMinutes;

    private String sessionStartTime; // HH:mm copied from OpdSession at booking time
    private int slotDuration; // minutes per slot, copied from OpdSession

    private boolean smsSent = false; // true once the 15-min reminder SMS has been dispatched

    // New fields for Doctor Allocation & Queue Generation Workflow
    private String allocationStatus = "PENDING"; // PENDING | ALLOCATED
    private String assignedDoctorId;
    private String finalQueueNumber; // e.g. "A-01"
    private String estimatedConsultationTime; // e.g. "09:15 AM"
    private LocalDateTime allocatedAt;
    private String liveQueueStatus; // e.g. "Currently Serving", "Waiting", "Next"
    private int priorityLevel = 0; // optional priority level

    @CreatedDate
    private LocalDateTime createdAt;
}
