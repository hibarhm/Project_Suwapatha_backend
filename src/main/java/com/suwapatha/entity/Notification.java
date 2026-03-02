package com.suwapatha.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String recipientId; // Can be doctorEmail or patientId
    private String title;
    private String message;

    // message, cancelled, lab, reminder
    private String type;

    // mail, calendar, flask
    private String icon;

    private boolean isRead;

    @CreatedDate
    private LocalDateTime createdAt;
}
