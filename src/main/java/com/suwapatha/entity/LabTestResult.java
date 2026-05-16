package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabTestResult {
    private String testName;
    private String value;
    private String unit;
    private String referenceRange;
    private String remarks;
}
