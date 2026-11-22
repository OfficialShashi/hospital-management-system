package com.hms.medicalrecord.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.appointment.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.doctor.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.medicalrecord.MedicalRecord;
import com.hms.medicalrecord.repository.MedicalRecordRepository;
import com.hms.patient.Patient;
import com.hms.patient.repository.PatientRepository;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository) {

        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Medical record not found"));
    }

    public List<MedicalRecord> getRecordsByPatient(Long patientId) {
        return medicalRecordRepository.findByPatient_PatientId(patientId);
    }

    public List<MedicalRecord> getRecordsByDoctor(Long doctorId) {
        return medicalRecordRepository.findByDoctor_DoctorId(doctorId);
    }

    public List<MedicalRecord> getRecordsByAppointment(Long appointmentId) {
        return medicalRecordRepository.findByAppointment_AppointmentId(appointmentId);
    }

    @Transactional
    public MedicalRecord createMedicalRecord(
            Long patientId,
            Long doctorId,
            Long appointmentId,
            String diagnosis,
            String symptoms,
            String treatment,
            String doctorNotes) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        Appointment appointment = null;

        if (appointmentId != null) {
            appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Appointment not found"));

            if (!appointment.getPatient().getPatientId().equals(patientId)) {
                throw new IllegalArgumentException(
                        "Appointment does not belong to this patient");
            }

            if (!appointment.getDoctor().getDoctorId().equals(doctorId)) {
                throw new IllegalArgumentException(
                        "Appointment does not belong to this doctor");
            }

            if (appointment.getAppointmentStatus()
                    != Appointment.AppointmentStatus.COMPLETED) {
                throw new IllegalArgumentException(
                        "Medical record can only be created for a completed appointment");
            }
        }

        MedicalRecord record = new MedicalRecord();

        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setAppointment(appointment);
        record.setDiagnosis(diagnosis);
        record.setSymptoms(symptoms);
        record.setTreatment(treatment);
        record.setDoctorNotes(doctorNotes);
        record.setRecordDate(LocalDateTime.now());

        return medicalRecordRepository.save(record);
    }
}