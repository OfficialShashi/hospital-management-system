
package com.hms.pharmacy.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hms.pharmacy.dto.MedicineDispensingRequest;
import com.hms.pharmacy.dto.MedicineDispensingResponse;
import com.hms.pharmacy.service.MedicineDispensingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medicine-dispensing")
@PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
public class MedicineDispensingController {

    private final MedicineDispensingService dispensingService;

    public MedicineDispensingController(
            MedicineDispensingService dispensingService) {
        this.dispensingService = dispensingService;
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @PostMapping
    public ResponseEntity<MedicineDispensingResponse> dispenseMedicine(
            @Valid @RequestBody MedicineDispensingRequest request) {

        MedicineDispensingResponse response =
                dispensingService.dispenseMedicine(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/prescription-item/{itemId}")
    public ResponseEntity<List<MedicineDispensingResponse>>
            getDispensingByPrescriptionItem(@PathVariable Long itemId) {

        List<MedicineDispensingResponse> responses =
                dispensingService.getDispensingByPrescriptionItem(itemId);

        return ResponseEntity.ok(responses);
    }
}
