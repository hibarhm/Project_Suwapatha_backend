package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    private String brandName;
    private String genericName;
    private String manufacturerName;
    private String dosageForm;
}
