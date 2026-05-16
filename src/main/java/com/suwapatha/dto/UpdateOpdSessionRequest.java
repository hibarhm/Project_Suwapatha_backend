package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOpdSessionRequest {
    private String doctorId;
    private String doctorEmail;
    private String doctorName;
    private String room;
    private String startTime;
    private String endTime;
    private String department;
    private Integer maxQueueSize;
    private String status; // OPEN | CANCELLED
}