package com.suwapatha.repository;

import com.suwapatha.entity.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    List<Appointment> findByPatientIdOrderByCreatedAtDesc(String patientId);

    Optional<Appointment> findFirstByPatientIdAndStatusOrderByCreatedAtDesc(String patientId, String status);

    int countByHospitalNameAndAppointmentDate(String hospitalName, String appointmentDate);

    List<Appointment> findByDoctorNameAndAppointmentDateAndStatusIn(String doctorName, String appointmentDate,
            List<String> statuses);

    Optional<Appointment> findFirstByPatientIdAndDoctorNameAndStatusInOrderByAppointmentDateDesc(String patientId, String doctorName, List<String> statuses);

    int countByDoctorNameAndAppointmentDate(String doctorName, String appointmentDate);

    long countByDoctorNameAndAppointmentDateBetween(String doctorName, String startDate, String endDate);

    List<Appointment> findByDoctorNameAndAppointmentDate(String doctorName, String appointmentDate);

    List<Appointment> findByDoctorName(String doctorName);

    List<Appointment> findBySessionIdOrderByQueueNumberAsc(String sessionId);

    /**
     * Used by SMS reminder scheduler: all BOOKED, not yet notified, with a session
     * start time stored
     */
    List<Appointment> findByStatusAndSmsSentFalseAndSessionStartTimeNotNull(String status);
}
