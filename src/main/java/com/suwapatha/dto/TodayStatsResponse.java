package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatsResponse {
    private int totalPatients;
    private int allocatedPatients;
    private int unallocatedPatients;
    private int activeDoctors;
    private int totalDoctors;
    private int activeSessions;
}