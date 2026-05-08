package com.suwapatha.service;

import com.suwapatha.dto.*;
import com.suwapatha.entity.*;
import com.suwapatha.exception.ResourceNotFoundException;
import com.suwapatha.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOpdService {

        private final OpdSessionRepository sessionRepository;
        private final HospitalRepository hospitalRepository;
        private final UserRepository userRepository;
        private final AppointmentRepository appointmentRepository;
        private final DoctorAvailabilityRepository availabilityRepository;

        /** Get the hospital assigned to this admin */
        public Hospital getAdminHospital(String adminEmail) {
                log.debug("Getting hospital for admin: {}", adminEmail);

                // Get admin user
                User admin = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> {
                                        log.error("Admin user not found for email: {}", adminEmail);
                                        return new ResourceNotFoundException("Admin user not found: " + adminEmail);
                                });

                log.debug("Found admin user: {}. Role: {}. Hospital ID: {}", admin.getEmail(), admin.getRole(),
                                admin.getHospitalId());

                // Verify user is admin
                if (!UserRole.ADMIN.equals(admin.getRole())) {
                        log.error("User {} is not an admin. Role: {}", adminEmail, admin.getRole());
                        throw new IllegalArgumentException("User is not an admin: " + adminEmail);
                }

                // Get hospital for this admin
                // First try findByAdminId
                Hospital hospital = hospitalRepository.findByAdminId(admin.getId())
                                .orElse(null);

                // Fallback: If not found by adminId, try finding by hospitalId from user record
                if (hospital == null && admin.getHospitalId() != null) {
                        log.info("Hospital not found by adminId, trying hospitalId from user record: {}",
                                        admin.getHospitalId());
                        hospital = hospitalRepository.findById(admin.getHospitalId()).orElse(null);
                }

                if (hospital == null) {
                        log.error("No hospital found for admin: {}", adminEmail);
                        throw new ResourceNotFoundException("No hospital assigned to this admin.");
                }

                log.debug("Admin {} manages hospital: {} ({})", adminEmail, hospital.getName(), hospital.getId());
                return hospital;
        }

        private String getTodayDate() {
                return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        /** Get hospital info */
        public HospitalInfoResponse getHospitalInfo(String adminEmail) {
                Hospital hospital = getAdminHospital(adminEmail);

                HospitalInfoResponse response = new HospitalInfoResponse();
                response.setId(hospital.getId());
                response.setName(hospital.getName());
                response.setLocation(hospital.getDistrict() + ", " + hospital.getProvince());
                response.setDistrict(hospital.getDistrict());
                response.setProvince(hospital.getProvince());
                response.setType(hospital.getType());
                response.setAddress(hospital.getAddress());
                response.setPhone(hospital.getPhone());

                return response;
        }

        /**
         * Get today's statistics for the admin's hospital
         */
        public TodayStatsResponse getTodayStats(String adminEmail) {
                Hospital hospital = getAdminHospital(adminEmail);
                String today = getTodayDate();

                log.debug("Getting today's stats for hospital: {}", hospital.getName());

                // Get today's sessions
                List<OpdSession> todaySessions = sessionRepository
                                .findByHospitalIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
                                                hospital.getId(),
                                                today,
                                                "OPEN")
                                .stream()
                                .filter(s -> s.getDate().equals(today))
                                .collect(Collectors.toList());

                // Calculate statistics
                int totalSlots = todaySessions.stream()
                                .mapToInt(OpdSession::getMaxQueueSize)
                                .sum();

                int allocatedPatients = todaySessions.stream()
                                .mapToInt(OpdSession::getCurrentQueueCount)
                                .sum();

                int unallocatedPatients = Math.max(0, totalSlots - allocatedPatients);

                int totalDoctors = todaySessions.size();

                int activeDoctors = (int) todaySessions.stream()
                                .filter(s -> s.getDoctorName() != null && !s.getDoctorName().isEmpty())
                                .filter(s -> s.getRoom() != null && !s.getRoom().isEmpty())
                                .count();

                long activeSessions = todaySessions.stream()
                                .filter(s -> "OPEN".equals(s.getStatus()))
                                .count();

                return TodayStatsResponse.builder()
                                .totalPatients(totalSlots)
                                .allocatedPatients(allocatedPatients)
                                .unallocatedPatients(unallocatedPatients)
                                .activeDoctors(activeDoctors)
                                .totalDoctors(totalDoctors)
                                .activeSessions((int) activeSessions)
                                .build();
        }

        /**
         * Get today's sessions for the admin's hospital
         */
        public List<OpdSessionResponse> getTodaySessions(String adminEmail) {
                Hospital hospital = getAdminHospital(adminEmail);
                String today = getTodayDate();

                log.debug("Getting today's sessions for hospital: {}", hospital.getName());

                List<OpdSession> sessions = sessionRepository
                                .findByHospitalIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
                                                hospital.getId(),
                                                today,
                                                "OPEN")
                                .stream()
                                .filter(s -> s.getDate().equals(today))
                                .collect(Collectors.toList());

                return sessions.stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get upcoming sessions (next 7 days) for the admin's hospital
         */
        public List<OpdSessionResponse> getUpcomingSessions(String adminEmail) {
                Hospital hospital = getAdminHospital(adminEmail);
                String today = getTodayDate();
                String endDate = LocalDate.now().plusDays(7)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                log.debug("Getting upcoming sessions for hospital: {}", hospital.getName());

                List<OpdSession> sessions = sessionRepository
                                .findByHospitalIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
                                                hospital.getId(),
                                                today,
                                                "OPEN")
                                .stream()
                                .filter(s -> s.getDate().compareTo(today) > 0 && s.getDate().compareTo(endDate) <= 0)
                                .collect(Collectors.toList());

                return sessions.stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Create new OPD session - hospital is automatically set from admin's
         * authentication
         */
        @Transactional
        public OpdSessionResponse createSession(String adminEmail, CreateOpdSessionRequest request) {
                Hospital hospital = getAdminHospital(adminEmail);

                log.info("Admin {} creating session for hospital: {}", adminEmail, hospital.getName());

                OpdSession session = new OpdSession();
                session.setHospitalId(hospital.getId());
                session.setHospitalName(hospital.getName());
                session.setDate(request.getDate());
                session.setStartTime(request.getStartTime());
                session.setEndTime(request.getEndTime());
                session.setDepartment(request.getDepartment());
                session.setDoctorName(request.getDoctorName() != null ? request.getDoctorName() : "");
                session.setRoom(request.getRoom() != null ? request.getRoom() : "");
                session.setMaxQueueSize(request.getMaxQueueSize());
                session.setSlotDuration(request.getSlotDuration());
                session.setCurrentQueueCount(0);
                session.setStatus("OPEN");

                session = sessionRepository.save(session);

                log.info("Session created successfully: {}", session.getId());

                return convertToResponse(session);
        }

        /**
         * Update existing session - verifies session belongs to admin's hospital
         */
        @Transactional
        public OpdSessionResponse updateSession(String adminEmail, String sessionId,
                        UpdateOpdSessionRequest request) {
                Hospital hospital = getAdminHospital(adminEmail);

                OpdSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

                // Security check: Verify session belongs to admin's hospital
                if (!session.getHospitalId().equals(hospital.getId())) {
                        throw new IllegalArgumentException(
                                        "You can only update sessions for your assigned hospital");
                }

                log.info("Admin {} updating session: {}", adminEmail, sessionId);

                // Update fields
                if (request.getDoctorName() != null) {
                        session.setDoctorName(request.getDoctorName());
                }
                if (request.getRoom() != null) {
                        session.setRoom(request.getRoom());
                }
                if (request.getStartTime() != null) {
                        session.setStartTime(request.getStartTime());
                }
                if (request.getEndTime() != null) {
                        session.setEndTime(request.getEndTime());
                }
                if (request.getDepartment() != null) {
                        session.setDepartment(request.getDepartment());
                }
                if (request.getMaxQueueSize() != null) {
                        session.setMaxQueueSize(request.getMaxQueueSize());
                }
                if (request.getStatus() != null) {
                        session.setStatus(request.getStatus());
                }

                session = sessionRepository.save(session);

                return convertToResponse(session);
        }

        /**
         * Assign room to session
         */
        @Transactional
        public OpdSessionResponse assignRoom(String adminEmail, String sessionId, String room) {
                Hospital hospital = getAdminHospital(adminEmail);

                OpdSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

                // Security check
                if (!session.getHospitalId().equals(hospital.getId())) {
                        throw new IllegalArgumentException(
                                        "Session does not belong to your hospital");
                }

                log.info("Assigning room {} to session {}", room, sessionId);

                session.setRoom(room);
                session = sessionRepository.save(session);

                return convertToResponse(session);
        }

        /**
         * Assign doctor to session
         */
        @Transactional
        public OpdSessionResponse assignDoctor(String adminEmail, String sessionId, String doctorName) {
                Hospital hospital = getAdminHospital(adminEmail);

                OpdSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

                // Security check
                if (!session.getHospitalId().equals(hospital.getId())) {
                        throw new IllegalArgumentException(
                                        "Session does not belong to your hospital");
                }

                log.info("Assigning doctor {} to session {}", doctorName, sessionId);

                session.setDoctorName(doctorName);
                session = sessionRepository.save(session);

                return convertToResponse(session);
        }

        /**
         * Update session status
         */
        @Transactional
        public OpdSessionResponse updateSessionStatus(String adminEmail, String sessionId, String status) {
                Hospital hospital = getAdminHospital(adminEmail);

                OpdSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

                // Security check
                if (!session.getHospitalId().equals(hospital.getId())) {
                        throw new IllegalArgumentException(
                                        "Session does not belong to your hospital");
                }

                log.info("Updating session {} status to: {}", sessionId, status);

                session.setStatus(status);
                session = sessionRepository.save(session);

                return convertToResponse(session);
        }

        /**
         * Cancel session - verifies session belongs to admin's hospital
         */
        @Transactional
        public void cancelSession(String adminEmail, String sessionId) {
                Hospital hospital = getAdminHospital(adminEmail);

                OpdSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

                // Security check
                if (!session.getHospitalId().equals(hospital.getId())) {
                        throw new IllegalArgumentException(
                                        "You can only cancel sessions for your assigned hospital");
                }

                log.info("Admin {} cancelling session: {}", adminEmail, sessionId);

                session.setStatus("CANCELLED");
                sessionRepository.save(session);
        }

        /**
         * Get patients for a specific session
         */
        public List<Appointment> getSessionPatients(String adminEmail, String sessionId) {
                Hospital hospital = getAdminHospital(adminEmail);

                OpdSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

                if (!session.getHospitalId().equals(hospital.getId())) {
                        throw new IllegalArgumentException("Session does not belong to your hospital");
                }

                // This is a simple implementation, you might want to map to a DTO
                return appointmentRepository.findBySessionIdOrderByQueueNumberAsc(sessionId);
        }

        /**
         * Get all sessions for admin's hospital
         */
        public List<OpdSessionResponse> getAllSessions(String adminEmail) {
                Hospital hospital = getAdminHospital(adminEmail);

                log.debug("Getting all sessions for hospital: {}", hospital.getName());

                List<OpdSession> sessions = sessionRepository
                                .findByHospitalIdOrderByDateDescStartTimeDesc(hospital.getId());

                return sessions.stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Get available rooms for the hospital
         */
        public List<String> getAvailableRooms(String adminEmail) {
                // In a real system, this would come from the hospital configuration
                // For now, returning a default list
                return List.of(
                                "OPD Room 1", "OPD Room 2", "OPD Room 3", "OPD Room 4",
                                "OPD Room 5", "OPD Room 6", "OPD Room 7", "OPD Room 8");
        }

        /**
         * Get all doctors available today for the admin's hospital
         */
        public List<DoctorAvailabilityResponse> getTodayAvailableDoctors(String adminEmail) {
                Hospital hospital = getAdminHospital(adminEmail);
                String today = getTodayDate();

                log.debug("Getting today's available doctors for hospital: {}", hospital.getName());

                // Fetch all doctors from this hospital
                List<String> hospitalDoctorEmails = userRepository
                                .findByRoleAndHospitalId(UserRole.DOCTOR, hospital.getId()).stream()
                                .map(User::getEmail)
                                .collect(Collectors.toList());

                // Fetch today's availability records that are marked as 'available'
                return availabilityRepository.findByDateAndAvailableTrue(today).stream()
                                .filter(a -> hospitalDoctorEmails.contains(a.getEmail())) // Only those from this
                                                                                          // hospital
                                .map(this::convertToAvailabilityResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Assign a room to a doctor's availability record for today
         */
        @Transactional
        public DoctorAvailabilityResponse assignDoctorRoom(String adminEmail, String availabilityId, String room) {
                log.info("Admin {} assigning room {} to availability {}", adminEmail, room, availabilityId);

                Hospital hospital = getAdminHospital(adminEmail);
                log.debug("Admin hospital ID: {}", hospital.getId());

                DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Availability record not found for ID: " + availabilityId));

                log.debug("Found availability record. Doctor ID in record: {}", availability.getDoctorId());

                if (availability.getDoctorId() == null) {
                        log.error("Doctor ID is null in availability record: {}", availabilityId);
                        throw new IllegalStateException("Doctor ID is missing in the availability record");
                }

                // Security check
                User doctor = userRepository.findById(availability.getDoctorId())
                                .orElseThrow(() -> {
                                        log.error("Doctor not found for ID: {} from availability {}",
                                                        availability.getDoctorId(), availabilityId);
                                        return new ResourceNotFoundException("Doctor user record not found for ID: "
                                                        + availability.getDoctorId());
                                });

                log.debug("Found doctor user: {} {}. Hospital ID in user: {}",
                                doctor.getFirstName(), doctor.getLastName(), doctor.getHospitalId());

                if (!hospital.getId().equals(doctor.getHospitalId())) {
                        log.error("Security violation: Admin hospital {} != Doctor hospital {}",
                                        hospital.getId(), doctor.getHospitalId());
                        throw new IllegalArgumentException("Doctor does not belong to your hospital");
                }

                log.info("Successfully assigning room {} to doctor {}", room, availability.getDoctorName());

                availability.setRoom(room);
                DoctorAvailability savedAvailability = availabilityRepository.save(availability);

                return convertToAvailabilityResponse(savedAvailability);
        }

        /**
         * Allocate patients in a session equally among active doctors with rooms
         */
        @Transactional
        public void allocatePatientsEqually(String adminEmail, String sessionId) {
                Hospital hospital = getAdminHospital(adminEmail);

                OpdSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

                if (!session.getHospitalId().equals(hospital.getId())) {
                        throw new IllegalArgumentException("Session does not belong to your hospital");
                }

                String today = getTodayDate();
                if (!session.getDate().equals(today)) {
                        throw new IllegalArgumentException("Allocation can only be done for today's sessions");
                }

                // 1. Get all active doctors for today with rooms assigned
                List<String> hospitalDoctorEmails = userRepository
                                .findByRoleAndHospitalId(UserRole.DOCTOR, hospital.getId()).stream()
                                .map(User::getEmail)
                                .collect(Collectors.toList());

                List<DoctorAvailability> activeDoctors = availabilityRepository.findByDateAndAvailableTrue(today)
                                .stream()
                                .filter(a -> hospitalDoctorEmails.contains(a.getEmail()))
                                .filter(a -> a.getRoom() != null && !a.getRoom().trim().isEmpty())
                                .collect(Collectors.toList());

                if (activeDoctors.isEmpty()) {
                        throw new IllegalArgumentException("No active doctors with assigned rooms found for today");
                }

                // 2. Get all booked appointments for this session
                List<Appointment> appointments = appointmentRepository.findBySessionIdOrderByQueueNumberAsc(sessionId)
                                .stream()
                                .filter(a -> "BOOKED".equals(a.getStatus()))
                                .collect(Collectors.toList());

                if (appointments.isEmpty()) {
                        log.info("No booked appointments to allocate for session {}", sessionId);
                        return;
                }

                log.info("Allocating {} patients among {} doctors for session {}",
                                appointments.size(), activeDoctors.size(), sessionId);

                // 3. Distribute patients equally / round-robin
                for (int i = 0; i < appointments.size(); i++) {
                        Appointment appointment = appointments.get(i);
                        DoctorAvailability assignedDoctor = activeDoctors.get(i % activeDoctors.size());

                        appointment.setDoctorName(assignedDoctor.getDoctorName());
                        appointment.setRoom(assignedDoctor.getRoom());

                        appointmentRepository.save(appointment);
                }

                // 4. Update session-level info if needed (optional, depends on UI expectations)
                // For now, we leave the session room as is, or mark it as "Distributed"
                session.setDoctorName("Multiple Doctors");
                session.setRoom("Distributed");
                sessionRepository.save(session);
        }

        private DoctorAvailabilityResponse convertToAvailabilityResponse(DoctorAvailability r) {
                return DoctorAvailabilityResponse.builder()
                                .id(r.getId())
                                .doctorId(r.getDoctorId())
                                .doctorName(r.getDoctorName())
                                .email(r.getEmail())
                                .date(r.getDate())
                                .available(r.isAvailable())
                                .note(r.getNote())
                                .room(r.getRoom())
                                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                                .build();
        }

        /**
         * Convert OpdSession entity to response DTO
         */
        private OpdSessionResponse convertToResponse(OpdSession session) {
                int availableSlots = session.getMaxQueueSize() - session.getCurrentQueueCount();

                OpdSessionResponse response = new OpdSessionResponse();
                response.setId(session.getId());
                response.setHospitalId(session.getHospitalId());
                response.setHospitalName(session.getHospitalName());
                response.setDate(session.getDate());
                response.setStartTime(session.getStartTime());
                response.setEndTime(session.getEndTime());
                response.setDepartment(session.getDepartment());
                response.setDoctorName(
                                session.getDoctorName() != null && !session.getDoctorName().isEmpty()
                                                ? session.getDoctorName()
                                                : "Not Assigned");
                response.setRoom(
                                session.getRoom() != null && !session.getRoom().isEmpty()
                                                ? session.getRoom()
                                                : "Not Assigned");
                response.setMaxQueueSize(session.getMaxQueueSize());
                response.setCurrentQueueCount(session.getCurrentQueueCount());
                response.setAvailableSlots(Math.max(0, availableSlots));
                response.setSlotDuration(session.getSlotDuration());
                response.setStatus(session.getStatus());

                return response;
        }
}