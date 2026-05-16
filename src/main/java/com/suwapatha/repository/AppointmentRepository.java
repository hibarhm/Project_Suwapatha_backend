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
    
    List<Appointment> findByPatientIdAndStatusInOrderByAppointmentDateAsc(String patientId, List<String> statuses);

    int countByHospitalNameAndAppointmentDate(String hospitalName, String appointmentDate);

    List<Appointment> findByDoctorNameAndAppointmentDateAndStatusIn(String doctorName, String appointmentDate,
            List<String> statuses);

    Optional<Appointment> findFirstByPatientIdAndDoctorNameAndStatusInOrderByAppointmentDateDesc(String patientId, String doctorName, List<String> statuses);

    int countByDoctorNameAndAppointmentDate(String doctorName, String appointmentDate);
    int countByDoctorEmailAndAppointmentDate(String doctorEmail, String appointmentDate);

    int countByDoctorNameAndAppointmentDateAndStatusIn(String doctorName, String appointmentDate, List<String> statuses);
    int countByDoctorEmailAndAppointmentDateAndStatusIn(String doctorEmail, String appointmentDate, List<String> statuses);

    long countByDoctorNameAndAppointmentDateBetween(String doctorName, String startDate, String endDate);
    long countByDoctorEmailAndAppointmentDateBetween(String doctorEmail, String startDate, String endDate);

    long countByDoctorNameAndAppointmentDateBetweenAndStatusIn(String doctorName, String startDate, String endDate, List<String> statuses);
    long countByDoctorEmailAndAppointmentDateBetweenAndStatusIn(String doctorEmail, String startDate, String endDate, List<String> statuses);

    int countByDoctorNameAndAppointmentDateBetweenAndStatus(String doctorName, String startDate, String endDate, String status);
    int countByDoctorEmailAndAppointmentDateBetweenAndStatus(String doctorEmail, String startDate, String endDate, String status);

    int countByDoctorNameAndAppointmentDateAndStatus(String doctorName, String appointmentDate, String status);
    int countByDoctorEmailAndAppointmentDateAndStatus(String doctorEmail, String appointmentDate, String status);

    List<Appointment> findByDoctorNameAndAppointmentDate(String doctorName, String appointmentDate);
    List<Appointment> findByDoctorEmailAndAppointmentDate(String doctorEmail, String appointmentDate);

    List<Appointment> findByDoctorName(String doctorName);

    List<Appointment> findByDoctorNameAndAppointmentDateBetweenAndStatus(String doctorName, String startDate, String endDate, String status);
    List<Appointment> findByDoctorEmailAndAppointmentDateBetweenAndStatus(String doctorEmail, String startDate, String endDate, String status);

    List<Appointment> findBySessionIdOrderByQueueNumberAsc(String sessionId);

    /**
     * Used by SMS reminder scheduler: all BOOKED, not yet notified, with a session
     * start time stored
     */
    List<Appointment> findByStatusAndSmsSentFalseAndSessionStartTimeNotNull(String status);

    int countByHospitalNameAndAppointmentDateBetweenAndStatus(String hospitalName, String startDate, String endDate, String status);
}
