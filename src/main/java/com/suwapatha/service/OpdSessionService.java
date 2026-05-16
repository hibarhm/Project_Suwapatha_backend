package com.suwapatha.service;

import com.suwapatha.dto.OpdSessionResponse;
import com.suwapatha.entity.Hospital;
import com.suwapatha.entity.OpdSession;
import com.suwapatha.repository.HospitalRepository;
import com.suwapatha.repository.OpdSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpdSessionService {

    private final OpdSessionRepository opdSessionRepository;
    private final HospitalRepository hospitalRepository;

    /** Patient: get upcoming OPEN sessions for a hospital */
    public List<OpdSessionResponse> getUpcomingSessionsForHospital(String hospitalId) {
        String today = LocalDate.now().toString();
        LocalTime nowTime = LocalTime.now();

        return opdSessionRepository
                .findByHospitalIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
                        hospitalId, today, "OPEN")
                .stream()
                .filter(s -> {
                    // Hide session from patients if it's today and after 8:00 PM
                    if (s.getDate().equals(today) && nowTime.isAfter(LocalTime.of(20, 0))) {
                        return false;
                    }
                    return true;
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Called by AppointmentService when a patient books — atomically increments
     * queue
     */
    public OpdSession incrementQueueAndGet(String sessionId) {
        OpdSession session = opdSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"OPEN".equals(session.getStatus())) {
            throw new RuntimeException("This session is no longer open for bookings.");
        }

        // Disable booking at 8:00 PM of the session day
        if (session.getDate().equals(LocalDate.now().toString()) && LocalTime.now().isAfter(LocalTime.of(20, 0))) {
            throw new RuntimeException("Bookings for this session are closed (Sessions are closed at 8:00 PM on the day of the session).");
        }
        if (session.getCurrentQueueCount() >= session.getMaxQueueSize()) {
            throw new RuntimeException("This session is fully booked.");
        }

        session.setCurrentQueueCount(session.getCurrentQueueCount() + 1);
        if (session.getCurrentQueueCount() >= session.getMaxQueueSize()) {
            session.setStatus("FULL");
        }

        return opdSessionRepository.save(session);
    }

    public OpdSessionResponse toResponse(OpdSession s) {
        OpdSessionResponse r = new OpdSessionResponse();
        r.setId(s.getId());
        r.setHospitalId(s.getHospitalId());
        r.setHospitalName(s.getHospitalName());
        r.setDate(s.getDate());
        r.setStartTime(s.getStartTime());
        r.setEndTime(s.getEndTime());
        r.setDepartment(s.getDepartment());
        r.setDoctorName(s.getDoctorName());
        r.setRoom(s.getRoom());
        r.setMaxQueueSize(s.getMaxQueueSize());
        r.setCurrentQueueCount(s.getCurrentQueueCount());
        r.setAvailableSlots(s.getMaxQueueSize() - s.getCurrentQueueCount());
        r.setSlotDuration(s.getSlotDuration());
        r.setStatus(s.getStatus());
        return r;
    }
}