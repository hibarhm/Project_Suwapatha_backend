package com.suwapatha.repository;

import com.suwapatha.entity.User;
import com.suwapatha.entity.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDoctorId(String doctorId);

    boolean existsByEmail(String email);

    boolean existsByDoctorId(String doctorId);

    List<User> findByRole(UserRole role);

    List<User> findByRoleAndHospitalId(UserRole role, String hospitalId);

    Optional<User> findByEmailAndRole(String email, UserRole role);
}
