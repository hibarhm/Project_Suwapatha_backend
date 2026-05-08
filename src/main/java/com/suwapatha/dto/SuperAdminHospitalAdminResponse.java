package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminHospitalAdminResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
}

