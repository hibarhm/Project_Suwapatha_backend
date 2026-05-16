package com.suwapatha.service;

import com.suwapatha.entity.LabRequest;
import com.suwapatha.entity.LabTestResult;
import com.suwapatha.repository.LabRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRequestRepository labRequestRepository;

    public LabRequest createLabRequest(LabRequest labRequest) {
        labRequest.setStatus(LabRequest.Status.PENDING);
        labRequest.setCreatedAt(LocalDateTime.now());
        return labRequestRepository.save(labRequest);
    }

    public List<LabRequest> getAllRequestsByHospital(String hospitalId) {
        return labRequestRepository.findByHospitalId(hospitalId);
    }

    public List<LabRequest> getRequestsByHospitalAndStatus(String hospitalId, LabRequest.Status status) {
        return labRequestRepository.findByHospitalIdAndStatus(hospitalId, status);
    }

    public LabRequest getRequestById(String id) {
        return labRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab request not found"));
    }

    public LabRequest updateStatus(String id, LabRequest.Status status) {
        LabRequest request = getRequestById(id);
        request.setStatus(status);
        request.setUpdatedAt(LocalDateTime.now());
        return labRequestRepository.save(request);
    }

    public LabRequest submitResults(String id, List<LabTestResult> results, List<String> reportUrls, String staffId) {
        LabRequest request = getRequestById(id);
        request.setResults(results);
        request.setReportUrls(reportUrls);
        request.setStatus(LabRequest.Status.COMPLETED);
        request.setCompletedBy(staffId);
        request.setCompletedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return labRequestRepository.save(request);
    }

    public List<LabRequest> getPatientLabHistory(String patientId) {
        return labRequestRepository.findByPatientId(patientId);
    }
}
