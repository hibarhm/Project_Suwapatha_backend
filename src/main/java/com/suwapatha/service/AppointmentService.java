package com.suwapatha.service;

import com.suwapatha.dto.AppointmentResponse;
import com.suwapatha.dto.BookAppointmentRequest;
import com.suwapatha.entity.Appointment;
import com.suwapatha.entity.OpdSession;
import com.suwapatha.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final OpdSessionService opdSessionService;

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

    /** Latest BOOKED appointment (for the queue status card) */
    public AppointmentResponse getActiveAppointment(String patientId) {
        return appointmentRepository
                .findFirstByPatientIdAndStatusOrderByCreatedAtDesc(patientId, "BOOKED")
                .map(this::toResponse)
                .orElse(null);
    }

    /** Cancel — only the owner can cancel */
    public void cancelAppointment(String appointmentId, String patientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("Not authorized to cancel this appointment");
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
        r.setStatus(a.getStatus());
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
