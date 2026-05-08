package com.suwapatha.service;

import com.suwapatha.entity.Appointment;
import com.suwapatha.entity.User;
import com.suwapatha.repository.AppointmentRepository;
import com.suwapatha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Runs every 60 seconds and sends an SMS reminder to each patient
 * whose queue turn is approximately 15 minutes away.
 *
 * Trigger condition:
 * now >= sessionStartTime(UTC) + estimatedWaitMinutes – 15 minutes
 *
 * The {@code smsSent} flag on {@link Appointment} ensures no patient
 * receives more than one reminder per appointment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    private static final int REMINDER_MINUTES_BEFORE = 15;

    @Scheduled(fixedDelay = 60_000) // runs every 60 seconds
    public void sendSmsReminders() {
        List<Appointment> pending = appointmentRepository
                .findByStatusAndSmsSentFalseAndSessionStartTimeNotNull("BOOKED");

        if (pending.isEmpty())
            return;

        Instant now = Instant.now();

        for (Appointment appt : pending) {
            try {
                Instant appointmentTime = computeAppointmentInstant(
                        appt.getAppointmentDate(),
                        appt.getSessionStartTime(),
                        appt.getEstimatedWaitMinutes());
                if (appointmentTime == null)
                    continue;

                Instant alertAt = appointmentTime.minusSeconds(REMINDER_MINUTES_BEFORE * 60L);

                if (!now.isBefore(alertAt)) { // now >= alertAt → time to send
                    String phone = resolvePatientPhone(appt.getPatientId());
                    if (phone == null) {
                        log.warn("Skipping SMS for appointment {} — patient has no phone number", appt.getId());
                    } else {
                        String msg = buildSmsText(appt);
                        smsService.sendSms(phone, msg);
                    }

                    // Mark as sent regardless of whether a phone was found,
                    // so we don't spam the log on every tick for missing numbers.
                    appt.setSmsSent(true);
                    appointmentRepository.save(appt);
                }
            } catch (Exception e) {
                log.error("Error processing SMS reminder for appointment {}: {}",
                        appt.getId(), e.getMessage());
            }
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Computes the patient's slot start time in UTC.
     * sessionDate: "yyyy-MM-dd", sessionStartTime: "HH:mm"
     */
    private Instant computeAppointmentInstant(String date, String startTime, int waitMinutes) {
        if (date == null || startTime == null)
            return null;
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalTime localTime = LocalTime.parse(startTime);
            Instant base = localDate.atTime(localTime).toInstant(ZoneOffset.UTC);
            return base.plusSeconds(waitMinutes * 60L);
        } catch (DateTimeParseException e) {
            log.warn("Cannot parse appointment time: date={}, startTime={}", date, startTime);
            return null;
        }
    }

    /** Looks up the patient's phone number from the User collection. */
    private String resolvePatientPhone(String patientId) {
        if (patientId == null)
            return null;
        return userRepository.findById(patientId)
                .map(User::getPhoneNumber)
                .filter(p -> p != null && !p.isBlank())
                .orElse(null);
    }

    /** Builds the SMS message text. */
    private String buildSmsText(Appointment appt) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u23F0 Suwapatha Reminder: Your appointment at ");
        sb.append(appt.getHospitalName());
        sb.append(" is in ~").append(REMINDER_MINUTES_BEFORE).append(" minutes");
        sb.append(" (Queue #").append(appt.getQueueNumber()).append(").");
        if (appt.getRoom() != null && !appt.getRoom().isBlank()) {
            sb.append(" Please proceed to Room ").append(appt.getRoom()).append(".");
        }
        sb.append(" \u2014 Suwapatha");
        return sb.toString();
    }
}
