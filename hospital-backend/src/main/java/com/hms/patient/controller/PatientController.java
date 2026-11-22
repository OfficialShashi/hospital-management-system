
package com.hms.patient.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hms.patient.dto.PatientUpdateRequestDTO;
import com.hms.patient.dto.PatientResponseDTO;
import com.hms.patient.service.PatientService;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:5173")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // Get all patients - ADMIN, RECEPTIONIST
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatientsDTO());
    }

    // Get patient by ID - ADMIN, RECEPTIONIST
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDTO> getPatientById(
            @PathVariable Long id) {

        return patientService.getPatientByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get patient by number - ADMIN, RECEPTIONIST
    @GetMapping("/number/{patientNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDTO> getPatientByNumber(
            @PathVariable String patientNumber) {

        return patientService.getPatientByNumberDTO(patientNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get patient by user ID - ownership checked for PATIENT
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<PatientResponseDTO> getPatientByUserId(
            @PathVariable Long userId,
            Authentication authentication) {

        return patientService
                .getPatientByUserIdDTO(userId, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update patient - ADMIN, RECEPTIONIST
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientUpdateRequestDTO updatedPatient) {

        patientService.updatePatient(id, updatedPatient);

        return patientService.getPatientByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get patient by email - ADMIN, RECEPTIONIST
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDTO> getPatientByEmail(
            @PathVariable String email) {

        return patientService.getPatientByEmailDTO(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Deactivate patient - ADMIN only
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponseDTO> deactivatePatient(
            @PathVariable Long id) {

        patientService.deactivatePatient(id);

        return patientService.getPatientByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Reactivate patient - ADMIN only
    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponseDTO> reactivatePatient(
            @PathVariable Long id) {

        patientService.reactivatePatient(id);

        return patientService.getPatientByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 // Get patients linked to logged-in doctor
    @GetMapping("/doctor/my-patients")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<PatientResponseDTO>> getMyPatients(
            Authentication authentication) {

        return ResponseEntity.ok(
                patientService.getPatientsByDoctor(authentication)
        );
    }


    // Get individual patient linked to logged-in doctor
    @GetMapping("/doctor/my-patients/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientResponseDTO> getMyPatientById(
            @PathVariable Long id,
            Authentication authentication) {

        return patientService.getDoctorPatientByIdDTO(id, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
