package com.hms.medicalrecord.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hms.medicalrecord.MedicalRecord;
import com.hms.medicalrecord.dto.MedicalRecordRequest;
import com.hms.medicalrecord.service.MedicalRecordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(
            MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    public ResponseEntity<MedicalRecord> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {

        MedicalRecord record = medicalRecordService.createMedicalRecord(
                request.getPatientId(),
                request.getDoctorId(),
                request.getAppointmentId(),
                request.getDiagnosis(),
                request.getSymptoms(),
                request.getTreatment(),
                request.getDoctorNotes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @GetMapping
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecords() {
        return ResponseEntity.ok(
                medicalRecordService.getAllMedicalRecords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> getMedicalRecordById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                medicalRecordService.getMedicalRecordById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> getRecordsByPatient(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(
                medicalRecordService.getRecordsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MedicalRecord>> getRecordsByDoctor(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(
                medicalRecordService.getRecordsByDoctor(doctorId));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<MedicalRecord>> getRecordsByAppointment(
            @PathVariable Long appointmentId) {

        return ResponseEntity.ok(
                medicalRecordService.getRecordsByAppointment(appointmentId));
    }
}