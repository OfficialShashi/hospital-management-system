
package com.hms.prescription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.hms.prescription.Prescription;
import com.hms.prescription.PrescriptionStatus;

@Repository
public interface PrescriptionRepository
        extends JpaRepository<Prescription, Long> {

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    boolean existsByPrescriptionNumber(String prescriptionNumber);

    @Override
    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    List<Prescription> findAll();

    @Override
    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    Optional<Prescription> findById(Long id);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    List<Prescription> findByPatient_PatientId(Long patientId);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    List<Prescription> findByDoctor_DoctorId(Long doctorId);

    List<Prescription> findByStatus(PrescriptionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prescription p WHERE p.prescriptionId = :id")
    Optional<Prescription> findByIdForUpdate(@Param("id") Long id);
}
