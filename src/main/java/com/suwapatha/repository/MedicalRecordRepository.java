package com.suwapatha.repository;

import com.suwapatha.entity.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {

    List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(String patientId);

    List<MedicalRecord> findByPatientIdAndVisitDateBetweenOrderByVisitDateDesc(
            String patientId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<MedicalRecord> findByPatientIdAndHospitalOrderByVisitDateDesc(
            String patientId,
            String hospital
    );
}