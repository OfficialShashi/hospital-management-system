
package com.hms.pharmacy.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hms.pharmacy.MedicineBatch;
import com.hms.pharmacy.dto.MedicineBatchRequest;
import com.hms.pharmacy.dto.MedicineBatchResponse;
import com.hms.pharmacy.dto.MedicineBatchResponseMapper;
import com.hms.pharmacy.service.MedicineBatchService;

import jakarta.validation.Valid;

@PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
@RestController
@RequestMapping("/api/medicine-batches")
public class MedicineBatchController {

    private final MedicineBatchService batchService;
    private final MedicineBatchResponseMapper mapper;

    public MedicineBatchController(
            MedicineBatchService batchService,
            MedicineBatchResponseMapper mapper) {
        this.batchService = batchService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<MedicineBatchResponse> createBatch(
            @Valid @RequestBody MedicineBatchRequest request) {

        MedicineBatch batch = batchService.createBatch(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(batch));
    }

    @GetMapping
    public ResponseEntity<List<MedicineBatchResponse>> getAllBatches() {

        List<MedicineBatchResponse> response =
                batchService.getAllBatches()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineBatchResponse> getBatchById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                mapper.toResponse(batchService.getBatchById(id)));
    }

    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<List<MedicineBatchResponse>> getBatchesByMedicine(
            @PathVariable Long medicineId) {

        List<MedicineBatchResponse> response =
                batchService.getBatchesByMedicine(medicineId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}
