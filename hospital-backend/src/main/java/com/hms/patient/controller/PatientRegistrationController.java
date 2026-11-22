
package com.hms.patient.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientRegistrationResponse;
import com.hms.patient.service.PatientRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:5173")
public class PatientRegistrationController {

    private final PatientRegistrationService registrationService;

    public PatientRegistrationController(
            PatientRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<PatientRegistrationResponse> registerPatient(
            @Valid @RequestBody PatientRegistrationRequest request) {

        PatientRegistrationResponse response =
                registrationService.registerPatient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
