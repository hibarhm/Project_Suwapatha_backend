package com.suwapatha.service;

import com.suwapatha.entity.Notification;
import com.suwapatha.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(String recipientId, String title, String message, String type, String icon) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .title(title)
                .message(message)
                .type(type)
                .icon(icon)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public void notifyDoctorRoomAllocation(String doctorEmail, String roomName) {
        createNotification(
                doctorEmail,
                "Room Allocated",
                "Admin has assigned room: " + roomName + " to you for today.",
                "alert",
                "calendar"
        );
    }

    public void notifyDoctorPatientAllocation(String doctorEmail, int patientCount, String sessionName) {
        createNotification(
                doctorEmail,
                "Patients Assigned",
                patientCount + " patients have been assigned to you in session: " + sessionName,
                "appointment",
                "calendar"
        );
    }
}
