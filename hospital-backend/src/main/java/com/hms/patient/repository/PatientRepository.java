
package com.hms.patient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.patient.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientNumber(String patientNumber);

    boolean existsByPatientNumber(String patientNumber);

    Optional<Patient> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);

    Optional<Patient> findByUser_Email(String email);
}
