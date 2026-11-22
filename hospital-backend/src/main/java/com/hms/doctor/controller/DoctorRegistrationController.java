package com.hms.doctor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.hms.doctor.dto.DoctorRegistrationRequest;
import com.hms.doctor.dto.DoctorRegistrationResponse;
import com.hms.doctor.service.DoctorRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorRegistrationController {

    private final DoctorRegistrationService doctorRegistrationService;

    public DoctorRegistrationController(
            DoctorRegistrationService doctorRegistrationService) {
        this.doctorRegistrationService = doctorRegistrationService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<DoctorRegistrationResponse> registerDoctor(
            @Valid @RequestBody DoctorRegistrationRequest request) {

        DoctorRegistrationResponse response =
                doctorRegistrationService.registerDoctor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}