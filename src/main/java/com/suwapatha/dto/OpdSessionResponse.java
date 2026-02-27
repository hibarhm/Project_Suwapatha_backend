package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpdSessionResponse {
    private String id;
    private String hospitalId;
    private String hospitalName;
    private String date;
    private String startTime;
    private String endTime;
    private String department;
    private String doctorName;
    private String room;
    private int maxQueueSize;
    private int currentQueueCount;
    private int availableSlots;
    private String status;
}
