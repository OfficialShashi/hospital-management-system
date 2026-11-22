
package com.hms.pharmacy.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hms.pharmacy.Medicine;
import com.hms.pharmacy.Medicine.MedicineStatus;
import com.hms.pharmacy.dto.MedicineRequest;
import com.hms.pharmacy.dto.MedicineResponse;
import com.hms.pharmacy.dto.MedicineResponseMapper;
import com.hms.pharmacy.service.MedicineService;

import jakarta.validation.Valid;

@PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;
    private final MedicineResponseMapper mapper;

    public MedicineController(
            MedicineService medicineService,
            MedicineResponseMapper mapper) {
        this.medicineService = medicineService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> createMedicine(
            @Valid @RequestBody MedicineRequest request) {

        Medicine medicine = medicineService.createMedicine(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(medicine));
    }

    @GetMapping
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {

        List<MedicineResponse> response = medicineService.getAllMedicines()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                mapper.toResponse(medicineService.getMedicineById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<MedicineResponse> getMedicineByCode(
            @PathVariable String code) {

        return ResponseEntity.ok(
                mapper.toResponse(medicineService.getMedicineByCode(code)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MedicineResponse>> getMedicinesByStatus(
            @PathVariable MedicineStatus status) {

        List<MedicineResponse> response =
                medicineService.getMedicinesByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MedicineResponse>> getMedicinesByCategory(
            @PathVariable String category) {

        List<MedicineResponse> response =
                medicineService.getMedicinesByCategory(category)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicineResponse>> searchMedicines(
            @RequestParam String name) {

        List<MedicineResponse> response =
                medicineService.searchMedicines(name)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request) {

        Medicine medicine = medicineService.updateMedicine(id, request);

        return ResponseEntity.ok(mapper.toResponse(medicine));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<MedicineResponse> updateMedicineStatus(
            @PathVariable Long id,
            @PathVariable MedicineStatus status) {

        Medicine medicine =
                medicineService.updateMedicineStatus(id, status);

        return ResponseEntity.ok(mapper.toResponse(medicine));
    }
}
