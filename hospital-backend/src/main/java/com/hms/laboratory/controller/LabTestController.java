
package com.hms.laboratory.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hms.laboratory.LabTest;
import com.hms.laboratory.dto.LabTestRequest;
import com.hms.laboratory.dto.LabTestResponse;
import com.hms.laboratory.dto.LabTestResponseMapper;
import com.hms.laboratory.service.LabTestService;

import jakarta.validation.Valid;

@PreAuthorize("hasAnyRole('ADMIN', 'LAB_TECHNICIAN', 'DOCTOR')")
@RestController
@RequestMapping("/api/lab-tests")
public class LabTestController {

    private final LabTestService labTestService;
    private final LabTestResponseMapper responseMapper;

    public LabTestController(
            LabTestService labTestService,
            LabTestResponseMapper responseMapper) {

        this.labTestService = labTestService;
        this.responseMapper = responseMapper;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LabTestResponse> createTest(
            @Valid @RequestBody LabTestRequest request) {

        LabTest savedTest = labTestService.createTest(convertToEntity(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(savedTest));
    }

    @GetMapping
    public ResponseEntity<List<LabTestResponse>> getAllTests() {

        List<LabTestResponse> response = labTestService.getAllTests()
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabTestResponse> getTestById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                responseMapper.toResponse(labTestService.getTestById(id)));
    }

    @GetMapping("/code/{testCode}")
    public ResponseEntity<LabTestResponse> getTestByCode(
            @PathVariable String testCode) {

        return ResponseEntity.ok(
                responseMapper.toResponse(
                        labTestService.getTestByCode(testCode)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LabTestResponse>> getTestsByStatus(
            @PathVariable LabTest.TestStatus status) {

        List<LabTestResponse> response = labTestService
                .getTestsByStatus(status)
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<LabTestResponse>> getTestsByCategory(
            @PathVariable String category) {

        List<LabTestResponse> response = labTestService
                .getTestsByCategory(category)
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LabTestResponse>> searchTests(
            @RequestParam String name) {

        List<LabTestResponse> response = labTestService
                .searchTests(name)
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LabTestResponse> updateTest(
            @PathVariable Long id,
            @Valid @RequestBody LabTestRequest request) {

        LabTest updatedTest = labTestService.updateTest(
                id, convertToEntity(request));

        return ResponseEntity.ok(responseMapper.toResponse(updatedTest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<LabTestResponse> updateTestStatus(
            @PathVariable Long id,
            @PathVariable LabTest.TestStatus status) {

        LabTest updatedTest = labTestService.updateTestStatus(id, status);

        return ResponseEntity.ok(responseMapper.toResponse(updatedTest));
    }

    private LabTest convertToEntity(LabTestRequest request) {

        LabTest labTest = new LabTest();

        labTest.setTestCode(request.getTestCode());
        labTest.setTestName(request.getTestName());
        labTest.setCategory(request.getCategory());
        labTest.setDescription(request.getDescription());
        labTest.setPrice(request.getPrice());
        labTest.setSampleType(request.getSampleType());
        labTest.setNormalRange(request.getNormalRange());
        labTest.setUnit(request.getUnit());
        labTest.setPreparationInstructions(
                request.getPreparationInstructions());

        return labTest;
    }
}
