package com.suwapatha.repository;

import com.suwapatha.entity.LabRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabRequestRepository extends MongoRepository<LabRequest, String> {
    List<LabRequest> findByPatientId(String patientId);
    List<LabRequest> findByStatus(LabRequest.Status status);
    List<LabRequest> findByHospitalId(String hospitalId);
    List<LabRequest> findByHospitalIdAndStatus(String hospitalId, LabRequest.Status status);
}
