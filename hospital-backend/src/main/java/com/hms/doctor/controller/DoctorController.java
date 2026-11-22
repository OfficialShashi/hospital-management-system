
package com.hms.doctor.controller;

import java.util.List;
import com.hms.doctor.dto.DoctorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hms.doctor.Doctor;
import com.hms.doctor.service.DoctorService;
import com.hms.doctor.dto.DoctorUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.hms.doctor.dto.DoctorCreateRequestDTO;


@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorCreateRequestDTO request) {

        DoctorResponseDTO doctor = doctorService.createDoctor(request);

        return ResponseEntity.status(201).body(doctor);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {

        return ResponseEntity.ok(doctorService.getAllDoctorsDTO());
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me")
    public ResponseEntity<DoctorResponseDTO> getMyDoctorProfile(
            Authentication authentication) {

        DoctorResponseDTO doctor =
                doctorService.getMyDoctorProfile(authentication);

        return ResponseEntity.ok(doctor);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @PathVariable Long id) {

        return doctorService.getDoctorByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<DoctorResponseDTO> getDoctorByLicenseNumber(
            @PathVariable String licenseNumber) {

        return doctorService.getDoctorByLicenseNumberDTO(licenseNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DoctorResponseDTO>> getDoctorsByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(
                doctorService.getDoctorsByDepartmentDTO(departmentId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping("/availability/{status}")
    public ResponseEntity<List<DoctorResponseDTO>> getDoctorsByAvailability(
            @PathVariable Doctor.AvailabilityStatus status) {

        return ResponseEntity.ok(
                doctorService.getDoctorsByAvailabilityDTO(status)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @PutMapping("/{id}/availability/{status}")
    public ResponseEntity<DoctorResponseDTO> updateAvailability(
            @PathVariable Long id,
            @PathVariable Doctor.AvailabilityStatus status,
            Authentication authentication) {

        DoctorResponseDTO updatedDoctor =
                doctorService.updateAvailabilityDTO(id, status, authentication);

        return ResponseEntity.ok(updatedDoctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request) {

        DoctorResponseDTO updatedDoctor =
                doctorService.updateDoctorDTO(id, request);

        return ResponseEntity.ok(updatedDoctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<DoctorResponseDTO> deactivateDoctor(
            @PathVariable Long id) {

        DoctorResponseDTO doctor =
                doctorService.deactivateDoctorDTO(id);

        return ResponseEntity.ok(doctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<DoctorResponseDTO> reactivateDoctor(
            @PathVariable Long id) {

        DoctorResponseDTO doctor =
                doctorService.reactivateDoctorDTO(id);

        return ResponseEntity.ok(doctor);
    }
}
