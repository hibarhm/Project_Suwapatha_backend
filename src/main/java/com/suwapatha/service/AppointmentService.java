package com.suwapatha.service;

import com.suwapatha.dto.AppointmentResponse;
import com.suwapatha.dto.BookAppointmentRequest;
import com.suwapatha.entity.Appointment;
import com.suwapatha.entity.OpdSession;
import com.suwapatha.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final OpdSessionService opdSessionService;
    private final UserService userService;

    /**
     * Books the patient into a specific OPD session.
     * Increments the session's queue count atomically.
     */
    public AppointmentResponse bookAppointment(String patientId, String patientEmail,
            BookAppointmentRequest request) {
        OpdSession session = opdSessionService.incrementQueueAndGet(request.getSessionId());

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setPatientEmail(patientEmail);
        appointment.setSessionId(session.getId());
        appointment.setHospitalName(session.getHospitalName());
        appointment.setAppointmentDate(session.getDate());
        appointment.setQueueNumber(session.getCurrentQueueCount());
        appointment.setDoctorId(session.getDoctorId());
        appointment.setDoctorEmail(session.getDoctorEmail());
        appointment.setDoctorName(session.getDoctorName());
        appointment.setRoom(session.getRoom());
        appointment.setStatus("BOOKED");

        // Estimated wait = (people ahead) * duration
        int ahead = Math.max(0, session.getCurrentQueueCount() - 1);
        int duration = session.getSlotDuration() > 0 ? session.getSlotDuration() : 15;
        appointment.setEstimatedWaitMinutes(ahead * duration);

        // Store session time info so the frontend can compute UTC appointment time
        appointment.setSessionStartTime(session.getStartTime());
        appointment.setSlotDuration(duration);

        return toResponse(appointmentRepository.save(appointment));
    }

    /** All appointments for the patient, newest first */
    public List<AppointmentResponse> getMyAppointments(String patientId) {
        return appointmentRepository
                .findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Latest relevant appointment (for the queue status card) */
    public AppointmentResponse getActiveAppointment(String patientId) {
        List<String> activeStatuses = List.of("BOOKED", "CHECKED_IN", "CONSULTING");
        List<Appointment> candidates = appointmentRepository
                .findByPatientIdAndStatusInOrderByAppointmentDateAsc(patientId, activeStatuses);

        LocalDate today = LocalDate.now();
        LocalTime noon = LocalTime.of(12, 0);
        LocalTime now = LocalTime.now();

        for (Appointment a : candidates) {
            try {
                LocalDate apptDate = LocalDate.parse(a.getAppointmentDate());
                
                // If the appointment is in the future, it's active
                if (apptDate.isAfter(today)) {
                    return toResponse(a);
                }
                
                // If it's today, check the 12 PM refresh rule
                if (apptDate.isEqual(today)) {
                    if (now.isBefore(noon)) {
                        return toResponse(a);
                    }
                    // If after 12 PM, we consider this session "finished" or "expired" for the card
                    continue;
                }
                
                // Past dates are ignored
            } catch (Exception e) {
                // If date parsing fails, skip it
            }
        }
        
        return null;
    }

    /** Cancel — only the owner can cancel */
    public void cancelAppointment(String appointmentId, String patientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("Not authorized to cancel this appointment");
        }

        // Penalty logic: check if cancellation is within 12 hours of session start
        try {
            LocalDate date = LocalDate.parse(appointment.getAppointmentDate());
            LocalTime time = LocalTime.parse(appointment.getSessionStartTime());
            LocalDateTime sessionTime = LocalDateTime.of(date, time);

            if (LocalDateTime.now().isAfter(sessionTime.minusHours(12))) {
                userService.incrementPenalty(patientId);
            }
        } catch (Exception e) {
            // Log error but proceed with cancellation if time parsing fails
            System.err.println("Error checking cancellation penalty: " + e.getMessage());
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }

    private AppointmentResponse toResponse(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.setId(a.getId());
        r.setHospitalName(a.getHospitalName());
        r.setAppointmentDate(a.getAppointmentDate());
        r.setQueueNumber(a.getQueueNumber());
        r.setDoctorName(a.getDoctorName());
        r.setRoom(a.getRoom());

        // Determine effective status based on time reset rules
        String status = a.getStatus();
        if ("BOOKED".equals(status) || "CHECKED_IN".equals(status)) {
            try {
                LocalDate apptDate = LocalDate.parse(a.getAppointmentDate());
                LocalDate today = LocalDate.now();
                LocalTime noon = LocalTime.of(12, 0);

                if (apptDate.isBefore(today) || (apptDate.isEqual(today) && LocalTime.now().isAfter(noon))) {
                    status = "FINISHED";
                }
            } catch (Exception e) {
                // date parse error, keep original status
            }
        }
        r.setStatus(status);
        r.setEstimatedWaitMinutes(a.getEstimatedWaitMinutes());
        r.setSessionStartTime(a.getSessionStartTime());
        r.setSlotDuration(a.getSlotDuration());
        r.setCreatedAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "");

        // Determine if this patient is "Next"
        if ("BOOKED".equals(a.getStatus()) || "CHECKED_IN".equals(a.getStatus())) {
            List<Appointment> sessionAppts = appointmentRepository.findBySessionIdOrderByQueueNumberAsc(a.getSessionId());
            int consultingQueue = -1;
            for (Appointment sa : sessionAppts) {
                if ("CONSULTING".equals(sa.getStatus())) {
                    consultingQueue = sa.getQueueNumber();
                    break;
                }
            }

            if (consultingQueue != -1) {
                // Find the first non-cancelled appointment after the consulting one
                for (Appointment sa : sessionAppts) {
                    if (sa.getQueueNumber() > consultingQueue &&
                            ("BOOKED".equals(sa.getStatus()) || "CHECKED_IN".equals(sa.getStatus()))) {
                        if (sa.getId().equals(a.getId())) {
                            r.setNext(true);
                        }
                        break; // Only the first one is "Next"
                    }
                }
            }
        }

        return r;
    }
}
