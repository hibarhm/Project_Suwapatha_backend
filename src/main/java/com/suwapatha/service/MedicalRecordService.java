package com.suwapatha.service;

import com.suwapatha.dto.MedicalRecordResponse;
import com.suwapatha.entity.MedicalRecord;
import com.suwapatha.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    public List<MedicalRecordResponse> getPatientMedicalRecords(String patientId) {
        List<MedicalRecord> records = medicalRecordRepository
                .findByPatientIdOrderByVisitDateDesc(patientId);

        return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<MedicalRecordResponse> getFilteredMedicalRecords(
            String patientId,
            String startDate,
            String endDate,
            String hospital
    ) {
        List<MedicalRecord> records;

        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            records = medicalRecordRepository
                    .findByPatientIdAndVisitDateBetweenOrderByVisitDateDesc(patientId, start, end);
        } else if (hospital != null && !hospital.isEmpty()) {
            records = medicalRecordRepository
                    .findByPatientIdAndHospitalOrderByVisitDateDesc(patientId, hospital);
        } else {
            records = medicalRecordRepository
                    .findByPatientIdOrderByVisitDateDesc(patientId);
        }

        return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private MedicalRecordResponse convertToResponse(MedicalRecord record) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        return MedicalRecordResponse.builder()
                .id(record.getId())
                .date(record.getVisitDate().format(dateFormatter))
                .time(record.getVisitTime())
                .hospital(record.getHospital())
                .doctor(record.getDoctorName())
                .diagnosis(record.getDiagnosis())
                .followUpRequired(record.isFollowUpRequired())
                .consultationNotes(record.getConsultationNotes())
                .bp(record.getBp())
                .temp(record.getTemp())
                .pulse(record.getPulse())
                .weight(record.getWeight())
                .prescriptions(record.getPrescriptions())
                .labReports(record.getLabReportUrls() != null ? record.getLabReportUrls().size() : 0)
                .labReportUrls(record.getLabReportUrls())
                .build();
    }
}