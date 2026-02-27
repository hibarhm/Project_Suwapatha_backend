package com.suwapatha.service;

import com.suwapatha.dto.CreateOpdSessionRequest;
import com.suwapatha.dto.OpdSessionResponse;
import com.suwapatha.dto.UpdateOpdSessionRequest;
import com.suwapatha.entity.Hospital;
import com.suwapatha.entity.OpdSession;
import com.suwapatha.repository.HospitalRepository;
import com.suwapatha.repository.OpdSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpdSessionService {

    private final OpdSessionRepository opdSessionRepository;
    private final HospitalRepository hospitalRepository;

    /** Admin: create a new OPD session */
    public OpdSessionResponse createSession(CreateOpdSessionRequest request) {
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found: " + request.getHospitalId()));

        OpdSession session = new OpdSession();
        session.setHospitalId(request.getHospitalId());
        session.setHospitalName(hospital.getName());
        session.setDate(request.getDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setDepartment(request.getDepartment());
        session.setDoctorName(request.getDoctorName() != null ? request.getDoctorName() : "");
        session.setRoom(request.getRoom() != null ? request.getRoom() : "");
        session.setMaxQueueSize(request.getMaxQueueSize());
        session.setCurrentQueueCount(0);
        session.setStatus("OPEN");

        return toResponse(opdSessionRepository.save(session));
    }

    /** Admin: update an existing session */
    public OpdSessionResponse updateSession(String sessionId, UpdateOpdSessionRequest request) {
        OpdSession session = opdSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (request.getDoctorName() != null)
            session.setDoctorName(request.getDoctorName());
        if (request.getRoom() != null)
            session.setRoom(request.getRoom());
        if (request.getStartTime() != null)
            session.setStartTime(request.getStartTime());
        if (request.getEndTime() != null)
            session.setEndTime(request.getEndTime());
        if (request.getDepartment() != null)
            session.setDepartment(request.getDepartment());
        if (request.getMaxQueueSize() != null)
            session.setMaxQueueSize(request.getMaxQueueSize());
        if (request.getStatus() != null)
            session.setStatus(request.getStatus());

        return toResponse(opdSessionRepository.save(session));
    }

    /** Admin: cancel a session */
    public void cancelSession(String sessionId) {
        OpdSession session = opdSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
        session.setStatus("CANCELLED");
        opdSessionRepository.save(session);
    }

    /** Admin: list all sessions */
    public List<OpdSessionResponse> getAllSessions() {
        return opdSessionRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Patient: get upcoming OPEN sessions for a hospital */
    public List<OpdSessionResponse> getUpcomingSessionsForHospital(String hospitalId) {
        String today = LocalDate.now().toString();
        return opdSessionRepository
                .findByHospitalIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
                        hospitalId, today, "OPEN")
                .stream().map(this::toResponse).collect(Collectors.toList());
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
        r.setStatus(s.getStatus());
        return r;
    }
}
