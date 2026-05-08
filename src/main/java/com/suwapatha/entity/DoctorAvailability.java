package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Tracks whether a doctor has marked themselves as available for a specific
 * date.
 * One document per doctor per date — upserted on each toggle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctor_availability")
@CompoundIndex(name = "doctor_date_idx", def = "{'doctorId': 1, 'date': 1}", unique = true)
public class DoctorAvailability {

    @Id
    private String id;

    private String doctorId; // User.id
    private String doctorName; // "Dr. First Last" — denormalised for display
    private String email;

    private String date; // yyyy-MM-dd
    private boolean available; // the toggle

    private String note; // optional free-text from doctor
    private String room; // assigned by admin

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
