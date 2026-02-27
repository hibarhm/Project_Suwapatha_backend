package com.suwapatha.repository;

import com.suwapatha.entity.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {

    List<Hospital> findByNameContainingIgnoreCase(String name);

    List<Hospital> findByDistrictIgnoreCase(String district);

    List<Hospital> findByProvinceIgnoreCase(String province);
}
