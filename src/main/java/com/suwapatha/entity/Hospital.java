package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "hospitals")
public class Hospital {

    @Id
    private String id;

    @Indexed
    private String name;

    private String district;
    private String province;
    private String type; // Teaching | Provincial General | Base | District | Specialized | Maternity
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;

    // Link to admin user
    @Indexed
    private String adminId; // References User.id where role = ADMIN
}