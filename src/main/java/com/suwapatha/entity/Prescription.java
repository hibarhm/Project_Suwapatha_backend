package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    private String medicine;
    private String dosage;
    private String frequency;
    private String duration;
    private String status; // Active, Completed
}