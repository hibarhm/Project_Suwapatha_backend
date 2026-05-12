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
import java.time.LocalTime;
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
        String today = LocalDate.now().toString();

        // Fetch only today's appointments (all statuses)
        return appointmentRepository.findByDoctorNameAndAppointmentDate(doctorName, today).stream()
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

        // Calculate time based on session start and queue number
        r.setTime(calculateAppointmentTime(a));

        userRepository.findByEmail(a.getPatientEmail()).ifPresent(u -> {
            r.setName(u.getFirstName() + " " + u.getLastName());
            r.setGender(u.getGender() != null ? u.getGender() : "Unknown");
        });

        return r;
    }

    private String calculateAppointmentTime(Appointment a) {
        if (a.getSessionStartTime() != null && !a.getSessionStartTime().isEmpty()) {
            try {
                LocalTime startTime = LocalTime.parse(a.getSessionStartTime());
                int offsetMinutes = (a.getQueueNumber() - 1) * a.getSlotDuration();
                return startTime.plusMinutes(offsetMinutes).format(DateTimeFormatter.ofPattern("hh:mm a"));
            } catch (Exception e) {
                log.warn("Error parsing session start time: {} for appointment {}", a.getSessionStartTime(), a.getId());
            }
        }
        return "N/A";
    }

    private DoctorDashboardResponse.Stats calculateStats(String doctorName, String today) {
        LocalDate now = LocalDate.now();
        String yesterday = now.minusDays(1).toString();

        int totalPatientsToday = appointmentRepository.countByDoctorNameAndAppointmentDate(doctorName, today);
        int totalPatientsYesterday = appointmentRepository.countByDoctorNameAndAppointmentDate(doctorName, yesterday);

        // Consultations this week
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        int consultationsThisWeek = (int) appointmentRepository.countByDoctorNameAndAppointmentDateBetween(
                doctorName, startOfWeek.toString(), today);

        // Last week
        LocalDate startOfLastWeek = startOfWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfWeek.minusDays(1);
        int consultationsLastWeek = (int) appointmentRepository.countByDoctorNameAndAppointmentDateBetween(
                doctorName, startOfLastWeek.toString(), endOfLastWeek.toString());

        // Last month
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);
        int consultationsThisMonth = (int) appointmentRepository.countByDoctorNameAndAppointmentDateBetween(
                doctorName, startOfMonth.toString(), today);
        int consultationsLastMonth = (int) appointmentRepository.countByDoctorNameAndAppointmentDateBetween(
                doctorName, startOfLastMonth.toString(), endOfLastMonth.toString());

        return DoctorDashboardResponse.Stats.builder()
                .totalPatientsToday(totalPatientsToday)
                .consultationsThisWeek(consultationsThisWeek)
                .averageWaitTime(15) // Still mocked as we don't have check-in/out duration data points yet
                .changeFromYesterday(calculatePercentChange(totalPatientsToday, totalPatientsYesterday))
                .changeFromLastWeek(calculatePercentChange(consultationsThisWeek, consultationsLastWeek))
                .changeFromLastMonth(calculatePercentChange(consultationsThisMonth, consultationsLastMonth))
                .build();
    }

    private int calculatePercentChange(int current, int previous) {
        if (previous == 0) return current > 0 ? 100 : 0;
        return (int) (((double) (current - previous) / previous) * 100);
    }

    private List<DoctorDashboardResponse.Notification> getRealNotifications(String doctorEmail) {
        // Fetch notifications from repository
        List<com.suwapatha.entity.Notification> notes = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(doctorEmail);

        return notes.stream()
                .map(n -> DoctorDashboardResponse.Notification.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .time(formatTime(n.getCreatedAt()))
                        .icon(n.getIcon())
                        .isRead(n.isRead())
                        .build())
                .collect(Collectors.toList());
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
        List<DoctorDashboardResponse.VisitData> data = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String start = month.withDayOfMonth(1).toString();
            String end = month.withDayOfMonth(month.lengthOfMonth()).toString();
            int count = (int) appointmentRepository.countByDoctorNameAndAppointmentDateBetween(doctorName, start, end);
            data.add(new DoctorDashboardResponse.VisitData(month.getMonth().name().substring(0, 3), count));
        }
        return data;
    }

    private List<DoctorDashboardResponse.DayConsultation> getConsultationsByDay(String doctorName) {
        List<DoctorDashboardResponse.DayConsultation> data = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            int count = appointmentRepository.countByDoctorNameAndAppointmentDate(doctorName, day.toString());
            data.add(new DoctorDashboardResponse.DayConsultation(day.getDayOfWeek().name().substring(0, 3), count));
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
        r.setTime(calculateAppointmentTime(a));
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
