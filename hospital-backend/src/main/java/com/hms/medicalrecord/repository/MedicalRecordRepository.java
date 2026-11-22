package com.hms.medicalrecord.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.medicalrecord.MedicalRecord;

@Repository
public interface MedicalRecordRepository
        extends JpaRepository<MedicalRecord, Long> {

    @Override
    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    List<MedicalRecord> findAll();

    @Override
    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    java.util.Optional<MedicalRecord> findById(Long id);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    List<MedicalRecord> findByPatient_PatientId(Long patientId);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    List<MedicalRecord> findByDoctor_DoctorId(Long doctorId);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user",
        "doctor",
        "doctor.user",
        "doctor.department",
        "appointment"
    })
    List<MedicalRecord> findByAppointment_AppointmentId(Long appointmentId);
}