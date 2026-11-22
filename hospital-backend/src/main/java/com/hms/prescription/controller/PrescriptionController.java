
package com.hms.prescription.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hms.prescription.Prescription;
import com.hms.prescription.PrescriptionItem;
import com.hms.prescription.PrescriptionStatus;
import com.hms.prescription.dto.PrescriptionItemRequest;
import com.hms.prescription.dto.PrescriptionItemResponse;
import com.hms.prescription.dto.PrescriptionItemResponseMapper;
import com.hms.prescription.dto.PrescriptionRequest;
import com.hms.prescription.dto.PrescriptionResponse;
import com.hms.prescription.dto.PrescriptionResponseMapper;
import com.hms.prescription.service.PrescriptionService;

import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionResponseMapper responseMapper;
    private final PrescriptionItemResponseMapper itemResponseMapper;

    public PrescriptionController(
            PrescriptionService prescriptionService,
            PrescriptionResponseMapper responseMapper,
            PrescriptionItemResponseMapper itemResponseMapper) {

        this.prescriptionService = prescriptionService;
        this.responseMapper = responseMapper;
        this.itemResponseMapper = itemResponseMapper;
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody PrescriptionRequest request) {

        List<PrescriptionItem> items = request.getItems()
                .stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        Prescription prescription = prescriptionService.createPrescription(
                request.getPatientId(),
                request.getDoctorId(),
                request.getAppointmentId(),
                request.getDiagnosis(),
                request.getInstructions(),
                items
        );

        PrescriptionResponse response =
                responseMapper.toResponse(prescription);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PrescriptionResponse>> getAllPrescriptions() {

        List<PrescriptionResponse> response =
                prescriptionService.getAllPrescriptions()
                        .stream()
                        .map(responseMapper::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(
            @PathVariable Long id) {

        Prescription prescription =
                prescriptionService.getPrescriptionById(id);

        return ResponseEntity.ok(responseMapper.toResponse(prescription));
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionByNumber(
            @PathVariable String number) {

        Prescription prescription =
                prescriptionService.getPrescriptionByNumber(number);

        return ResponseEntity.ok(responseMapper.toResponse(prescription));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByPatient(
            @PathVariable Long patientId) {

        List<PrescriptionResponse> response =
                prescriptionService.getPrescriptionsByPatient(patientId)
                        .stream()
                        .map(responseMapper::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByDoctor(
            @PathVariable Long doctorId) {

        List<PrescriptionResponse> response =
                prescriptionService.getPrescriptionsByDoctor(doctorId)
                        .stream()
                        .map(responseMapper::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{prescriptionId}/items")
    public ResponseEntity<List<PrescriptionItemResponse>> getPrescriptionItems(
            @PathVariable Long prescriptionId) {

        List<PrescriptionItemResponse> response =
                prescriptionService.getPrescriptionItems(prescriptionId)
                        .stream()
                        .map(itemResponseMapper::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PHARMACIST')")
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<PrescriptionResponse> updatePrescriptionStatus(
            @PathVariable Long id,
            @PathVariable PrescriptionStatus status) {

        PrescriptionResponse response =
                prescriptionService.updatePrescriptionStatus(id, status);

        return ResponseEntity.ok(response);
    }

    private PrescriptionItem convertToEntity(
            PrescriptionItemRequest request) {

        PrescriptionItem item = new PrescriptionItem();
        item.setMedicineName(request.getMedicineName());
        item.setDosage(request.getDosage());
        item.setRoute(request.getRoute());
        item.setFrequency(request.getFrequency());
        item.setDurationDays(request.getDurationDays());
        item.setQuantity(request.getQuantity());
        item.setInstructions(request.getInstructions());

        return item;
    }
}
