package com.suwapatha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyHospitalResponse {
    private String id;
    private String name;
    private String address;
    private String type;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
}
