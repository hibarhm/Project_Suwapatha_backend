package com.suwapatha.service;

import com.suwapatha.dto.AppointmentResponse;
import com.suwapatha.dto.ConsultationRequest;
import com.suwapatha.dto.DoctorAvailabilityResponse;
import com.suwapatha.dto.DoctorDashboardResponse;
import com.suwapatha.dto.DoctorPatientResponse;
import com.suwapatha.dto.PatientDetailsResponse;
import com.suwapatha.entity.Appointment;
import com.suwapatha.entity.DoctorAvailability;
import com.suwapatha.entity.MedicalRecord;
import com.suwapatha.entity.OpdSession;
import com.suwapatha.entity.Prescription;
import com.suwapatha.entity.User;
import com.suwapatha.repository.AppointmentRepository;
import com.suwapatha.repository.DoctorAvailabilityRepository;
import com.suwapatha.repository.MedicalRecordRepository;
import com.suwapatha.repository.NotificationRepository;
import com.suwapatha.repository.OpdSessionRepository;
import com.suwapatha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorDashboardService {

    private final AppointmentRepository appointmentRepository;
    private final OpdSessionRepository opdSessionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    public DoctorDashboardResponse getDashboardData(String doctorEmail) {
        log.info("Fetching dashboard data for doctor: {}", doctorEmail);

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!"APPROVED".equals(doctor.getStatus())) {
            log.warn("Doctor account not approved: {}. Status: {}", doctorEmail, doctor.getStatus());
            throw new RuntimeException("Account not approved. Your status is: " + doctor.getStatus());
        }

        String doctorName = doctor.getFirstName() + " " + doctor.getLastName();
        String today = LocalDate.now().toString();

        // 1. Stats
        DoctorDashboardResponse.Stats stats = calculateStats(doctorName, today);

        // 2. Upcoming Appointments (Today's booked/checked-in)
        List<AppointmentResponse> upcoming = appointmentRepository
                .findByDoctorNameAndAppointmentDateAndStatusIn(doctorName, today, Arrays.asList("BOOKED", "CHECKED_IN"))
                .stream()
                .map(this::toAppointmentResponse)
                .collect(Collectors.toList());

        // 3. Notifications
        List<DoctorDashboardResponse.Notification> notifications = getRealNotifications(doctorEmail);

        // 4. Patient Visits Over Time (Last 6 months)
        List<DoctorDashboardResponse.VisitData> visitData = getVisitData(doctorName);

        // 5. Consultations by Day (Current week)
        List<DoctorDashboardResponse.DayConsultation> consultationsByDay = getConsultationsByDay(doctorName);

        return DoctorDashboardResponse.builder()
                .stats(stats)
                .upcomingAppointments(upcoming)
                .notifications(notifications)
                .patientVisitsData(visitData)
                .consultationsByDay(consultationsByDay)
                .build();
    }

    public List<DoctorPatientResponse> getDoctorPatients(String doctorEmail) {
        log.info("Fetching patient list for doctor: {}", doctorEmail);

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String doctorName = doctor.getFirstName() + " " + doctor.getLastName();

        return appointmentRepository.findByDoctorName(doctorName).stream()
                .map(this::toDoctorPatientResponse)
                .collect(Collectors.toList());
    }

    public PatientDetailsResponse getPatientDetails(String patientId, String doctorEmail) {
        log.info("Fetching details for patient: {} by doctor: {}", patientId, doctorEmail);

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<MedicalRecord> history = medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);

        // Get active prescriptions from the latest record if any
        List<Prescription> activePrescriptions = history.isEmpty() ? new ArrayList<>()
                : history.get(0).getPrescriptions().stream()
                        .filter(p -> "Active".equals(p.getStatus()))
                        .collect(Collectors.toList());

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        String doctorName = doctor.getFirstName() + " " + doctor.getLastName();

        // Find current active appointment for this doctor and patient
        Optional<Appointment> currentAppt = appointmentRepository
                .findFirstByPatientIdAndDoctorNameAndStatusInOrderByAppointmentDateDesc(
                        patientId, doctorName, Arrays.asList("BOOKED", "CHECKED_IN", "CONSULTING"));

        return PatientDetailsResponse.builder()
                .id(patient.getId())
                .name(patient.getFirstName() + " " + patient.getLastName())
                .age(calculateAge(patient.getDateOfBirth()))
                .gender(patient.getGender())
                .bloodType(patient.getBloodType())
                .phone(patient.getPhoneNumber())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .emergencyContact(patient.getEmergencyContact())
                .allergies(new ArrayList<>()) // To be implemented if we add allergies field to User
                .chronicConditions(new ArrayList<>()) // To be implemented
                .medicalHistory(history)
                .activePrescriptions(activePrescriptions)
                .currentAppointmentId(currentAppt.map(Appointment::getId).orElse(null))
                .currentStatus(currentAppt.map(Appointment::getStatus).orElse(null))
                .hospitalName(currentAppt.map(Appointment::getHospitalName).orElse(null))
                .build();
    }

    public void updateAppointmentStatus(String appointmentId, String status, String doctorEmail) {
        log.info("Updating appointment: {} to status: {} by doctor: {}", appointmentId, status, doctorEmail);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        String doctorName = doctor.getFirstName() + " " + doctor.getLastName();

        if (!appointment.getDoctorName().equals(doctorName)) {
            throw new RuntimeException("Unauthorized: You are not the assigned doctor for this appointment");
        }

        appointment.setStatus(status);
        appointmentRepository.save(appointment);
    }

    public void saveConsultation(ConsultationRequest request, String doctorEmail) {
        log.info("Saving consultation for patient: {} by doctor: {}", request.getPatientId(), doctorEmail);

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patient.getId());
        record.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        record.setVisitDate(LocalDateTime.now());
        record.setDoctorId(doctor.getEmail());
        record.setDoctorName(doctor.getFirstName() + " " + doctor.getLastName());
        record.setDiagnosis(request.getDiagnosis());
        record.setConsultationNotes(request.getConsultationNotes());
        record.setFollowUpRequired(request.isFollowUpRequired());

        // Vitals
        record.setBp(request.getBp());
        record.setTemp(request.getTemp());
        record.setPulse(request.getPulse());
        record.setWeight(request.getWeight());

        record.setPrescriptions(request.getPrescriptions());
        record.setHospital(request.getHospitalName());
        record.setVisitTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        record.setCreatedAt(LocalDateTime.now());

        medicalRecordRepository.save(record);

        // Update appointment status to COMPLETED if appointmentId is provided
        if (request.getAppointmentId() != null && !request.getAppointmentId().isEmpty()) {
            appointmentRepository.findById(request.getAppointmentId()).ifPresent(a -> {
                a.setStatus("COMPLETED");
                appointmentRepository.save(a);
                log.info("Appointment {} marked as COMPLETED after consultation save", request.getAppointmentId());
            });
        }

        // If follow up required, maybe create a notification or similar logic
    }

    private String calculateAge(String dob) {
        if (dob == null || dob.isEmpty())
            return "N/A";
        try {
            LocalDate birthDate = LocalDate.parse(dob);
            return String.valueOf(java.time.Period.between(birthDate, LocalDate.now()).getYears());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private DoctorPatientResponse toDoctorPatientResponse(Appointment a) {
        DoctorPatientResponse r = new DoctorPatientResponse();
        r.setId(a.getId());
        r.setQueueNo("Q-" + String.format("%03d", a.getQueueNumber()));
        r.setStatus(a.getStatus());
        r.setPatientId(a.getPatientId());

        // Mock time if not available in appointment
        r.setTime("09:00 AM");

        userRepository.findByEmail(a.getPatientEmail()).ifPresent(u -> {
            r.setName(u.getFirstName() + " " + u.getLastName());
            r.setGender(u.getGender() != null ? u.getGender() : "Unknown");
        });

        return r;
    }

    private DoctorDashboardResponse.Stats calculateStats(String doctorName, String today) {
        int totalPatientsToday = appointmentRepository.countByDoctorNameAndAppointmentDate(doctorName, today);

        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        int consultationsThisWeek = (int) appointmentRepository.countByDoctorNameAndAppointmentDateBetween(
                doctorName, startOfWeek.toString(), today);

        // Mocking some values to match the dashboard's needs (change percentages etc.)
        return DoctorDashboardResponse.Stats.builder()
                .totalPatientsToday(totalPatientsToday)
                .consultationsThisWeek(consultationsThisWeek)
                .averageWaitTime(15) // Mocked or calculated if we have data
                .changeFromYesterday(5)
                .changeFromLastWeek(12)
                .changeFromLastMonth(-2)
                .build();
    }

    private List<DoctorDashboardResponse.Notification> getRealNotifications(String doctorEmail) {
        List<com.suwapatha.entity.Notification> notes = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(doctorEmail);

        // Seed if empty for testing purposes
        if (notes.isEmpty()) {
            seedInitialNotifications(doctorEmail);
            notes = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(doctorEmail);
        }

        return notes.stream()
                .map(n -> DoctorDashboardResponse.Notification.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .time(formatTime(n.getCreatedAt()))
                        .icon(n.getIcon())
                        .build())
                .collect(Collectors.toList());
    }

    private void seedInitialNotifications(String doctorEmail) {
        notificationRepository.save(com.suwapatha.entity.Notification.builder()
                .recipientId(doctorEmail)
                .title("New message from Patient regarding lab results.")
                .type("message")
                .icon("mail")
                .isRead(false)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build());

        notificationRepository.save(com.suwapatha.entity.Notification.builder()
                .recipientId(doctorEmail)
                .title("Appointment with John Smith cancelled at 10:30 AM.")
                .type("cancelled")
                .icon("calendar")
                .isRead(false)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());

        notificationRepository.save(com.suwapatha.entity.Notification.builder()
                .recipientId(doctorEmail)
                .title("Lab report for Emily White is ready for review.")
                .type("lab")
                .icon("flask")
                .isRead(false)
                .createdAt(LocalDateTime.now().minusHours(3))
                .build());
    }

    private String formatTime(LocalDateTime dt) {
        if (dt == null)
            return "Just now";
        // Simple relative time formatting
        LocalDateTime now = LocalDateTime.now();
        if (dt.plusMinutes(60).isAfter(now)) {
            return "Just now";
        } else if (dt.plusHours(24).isAfter(now)) {
            return (now.getHour() - dt.getHour()) + " hours ago";
        }
        return dt.format(DateTimeFormatter.ofPattern("MMM dd"));
    }

    private List<DoctorDashboardResponse.VisitData> getVisitData(String doctorName) {
        // Mocking for now, could be aggregated from AppointmentRepository
        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun" };
        int[] visits = { 190, 230, 210, 280, 250, 310 };

        List<DoctorDashboardResponse.VisitData> data = new ArrayList<>();
        for (int i = 0; i < months.length; i++) {
            data.add(new DoctorDashboardResponse.VisitData(months[i], visits[i]));
        }
        return data;
    }

    private List<DoctorDashboardResponse.DayConsultation> getConsultationsByDay(String doctorName) {
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        int[] counts = { 18, 24, 21, 32, 23, 15, 10 };

        List<DoctorDashboardResponse.DayConsultation> data = new ArrayList<>();
        for (int i = 0; i < days.length; i++) {
            data.add(new DoctorDashboardResponse.DayConsultation(days[i], counts[i]));
        }
        return data;
    }

    private AppointmentResponse toAppointmentResponse(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.setId(a.getId());
        r.setHospitalName(a.getHospitalName());
        r.setAppointmentDate(a.getAppointmentDate());
        r.setQueueNumber(a.getQueueNumber());
        r.setQueueNo("A-" + String.format("%03d", a.getQueueNumber())); // Formatting example

        // Fetch patient name
        userRepository.findByEmail(a.getPatientEmail()).ifPresent(u -> {
            r.setPatientName(u.getFirstName() + " " + u.getLastName());
        });

        r.setDoctorName(a.getDoctorName());
        r.setRoom(a.getRoom());
        r.setStatus(a.getStatus());
        r.setEstimatedWaitMinutes(a.getEstimatedWaitMinutes());
        r.setTime("10:30 AM"); // Should be derived from session if available
        r.setAvatar(""); // Optional avatar URL
        r.setCreatedAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "");
        return r;
    }

    // ── Availability ──────────────────────────────────────────────────────────

    /**
     * Returns today's availability record for the logged-in doctor (never null).
     */
    public DoctorAvailabilityResponse getMyAvailabilityToday(String email) {
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        String today = LocalDate.now().toString();
        DoctorAvailability rec = availabilityRepository
                .findByDoctorIdAndDate(doctor.getId(), today)
                .orElse(DoctorAvailability.builder()
                        .doctorId(doctor.getId())
                        .doctorName(doctor.getFirstName() + " " + doctor.getLastName())
                        .email(email)
                        .date(today)
                        .available(false)
                        .build());
        return toAvailabilityResponse(rec);
    }

    /**
     * Upserts today's availability record for the doctor.
     * 
     * @param email     logged-in doctor email
     * @param available true = marking in, false = marking out
     * @param note      optional note
     */
    public DoctorAvailabilityResponse setMyAvailability(String email, boolean available, String note) {
        User doctor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        String today = LocalDate.now().toString();

        DoctorAvailability rec = availabilityRepository
                .findByDoctorIdAndDate(doctor.getId(), today)
                .orElseGet(() -> DoctorAvailability.builder()
                        .doctorId(doctor.getId())
                        .doctorName(doctor.getFirstName() + " " + doctor.getLastName())
                        .email(email)
                        .date(today)
                        .build());

        rec.setAvailable(available);
        rec.setNote(note);
        availabilityRepository.save(rec);
        log.info("Doctor {} marked {} for {}", email, available ? "AVAILABLE" : "UNAVAILABLE", today);
        return toAvailabilityResponse(rec);
    }

    private DoctorAvailabilityResponse toAvailabilityResponse(DoctorAvailability r) {
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
}
