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
@Document(collection = "opd_sessions")
public class OpdSession {

    @Id
    private String id;

    private String hospitalId;
    private String hospitalName;

    private String date; // yyyy-MM-dd
    private String startTime; // HH:mm
    private String endTime; // HH:mm

    private String department;
    private String doctorName;
    private String room;

    private int maxQueueSize;
    private int currentQueueCount;
    private int slotDuration;

    // OPEN | FULL | CANCELLED
    private String status;

    @CreatedDate
    private LocalDateTime createdAt;
}