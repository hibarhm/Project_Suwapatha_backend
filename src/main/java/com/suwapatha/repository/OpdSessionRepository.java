package com.suwapatha.repository;

import com.suwapatha.entity.OpdSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpdSessionRepository extends MongoRepository<OpdSession, String> {

    List<OpdSession> findByHospitalIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
            String hospitalId, String date, String status);

    List<OpdSession> findByHospitalIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
            String hospitalId, String date);

    List<OpdSession> findByHospitalIdOrderByDateDescStartTimeDesc(String hospitalId);

    List<OpdSession> findByHospitalIdAndDateBetweenOrderByDateDescStartTimeDesc(
            String hospitalId, String startDate, String endDate);

    List<OpdSession> findByStatusIn(List<String> statuses);

    long countByHospitalIdAndDateBetweenAndStatus(String hospitalId, String startDate, String endDate, String status);

    long countByHospitalIdAndDateBetween(String hospitalId, String startDate, String endDate);
}