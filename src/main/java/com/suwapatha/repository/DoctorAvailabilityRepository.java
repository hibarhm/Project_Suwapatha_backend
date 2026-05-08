package com.suwapatha.repository;

import com.suwapatha.entity.DoctorAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends MongoRepository<DoctorAvailability, String> {

    /** Fetch the single record for a doctor on a given day. */
    Optional<DoctorAvailability> findByDoctorIdAndDate(String doctorId, String date);

    /** Fetch all doctors who toggled themselves available on a given day. */
    List<DoctorAvailability> findByDateAndAvailableTrue(String date);
}
