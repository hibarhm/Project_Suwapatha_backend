package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponse {
    private String id;
    private String name;
    private String district;
    private String province;
    private String type;
    private String address;
    private String phone;
}
