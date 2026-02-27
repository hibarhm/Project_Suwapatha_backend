package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private String id;
    private String hospitalName;
    private String appointmentDate;
    private int queueNumber;
    private String doctorName;
    private String room;
    private String status;
    private int estimatedWaitMinutes;
    private String createdAt;
}
