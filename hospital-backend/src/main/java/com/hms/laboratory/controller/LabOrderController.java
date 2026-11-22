
package com.hms.laboratory.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hms.laboratory.dto.LabTestResultRequest;
import jakarta.validation.Valid;
import com.hms.laboratory.LabOrder;
import com.hms.laboratory.dto.LabOrderRequest;
import com.hms.laboratory.dto.LabOrderResponse;
import com.hms.laboratory.dto.LabOrderResponseMapper;
import com.hms.laboratory.service.LabOrderService;
import org.springframework.security.core.Authentication;


@PreAuthorize("hasAnyRole('ADMIN', 'LAB_TECHNICIAN')")
@RestController
@RequestMapping("/api/lab-orders")
public class LabOrderController {

    private final LabOrderService labOrderService;
    private final LabOrderResponseMapper responseMapper;

    public LabOrderController(
            LabOrderService labOrderService,
            LabOrderResponseMapper responseMapper) {

        this.labOrderService = labOrderService;
        this.responseMapper = responseMapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LabOrderResponse> createLabOrder(
            @Valid @RequestBody LabOrderRequest request) {

        LabOrder order = labOrderService.createLabOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(order));
    }

 // Doctor Creates Lab Order
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor")
    public ResponseEntity<LabOrderResponse> createLabOrderByDoctor(
            @Valid @RequestBody LabOrderRequest request,
            Authentication authentication) {

        LabOrder order = labOrderService.createLabOrderForDoctor(
                request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(order));
    }


 // Get Lab Orders For Logged-in Doctor
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/my-orders")
    public ResponseEntity<List<LabOrderResponse>> getMyLabOrders(
            Authentication authentication) {

        List<LabOrder> orders =
                labOrderService.getMyLabOrders(authentication.getName());

        List<LabOrderResponse> response = orders.stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LabOrderResponse>> getAllLabOrders() {

        List<LabOrderResponse> response = labOrderService
                .getAllLabOrders()
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabOrderResponse> getLabOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                responseMapper.toResponse(
                        labOrderService.getLabOrderById(id)));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<LabOrderResponse> getLabOrderByNumber(
            @PathVariable String orderNumber) {

        return ResponseEntity.ok(
                responseMapper.toResponse(
                        labOrderService.getLabOrderByNumber(orderNumber)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<LabOrderResponse>> getOrdersByPatient(
            @PathVariable Long patientId) {

        List<LabOrderResponse> response = labOrderService
                .getOrdersByPatient(patientId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<LabOrderResponse>> getOrdersByDoctor(
            @PathVariable Long doctorId) {

        List<LabOrderResponse> response = labOrderService
                .getOrdersByDoctor(doctorId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LabOrderResponse>> getOrdersByStatus(
            @PathVariable LabOrder.OrderStatus status) {

        List<LabOrderResponse> response = labOrderService
                .getOrdersByStatus(status)
                .stream()
                .map(responseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // Collect sample for a lab order item

	@PutMapping("/items/{itemId}/collect-sample")
	public ResponseEntity<LabOrderResponse> collectSample(
	        @PathVariable Long itemId) {

	    LabOrder order = labOrderService.collectSample(itemId);

	    return ResponseEntity.ok(responseMapper.toResponse(order));
	}


	@PutMapping("/items/{itemId}/start-processing")
	public ResponseEntity<LabOrderResponse> startProcessing(
	        @PathVariable Long itemId) {

	    LabOrder order = labOrderService.startProcessing(itemId);

	    return ResponseEntity.ok(responseMapper.toResponse(order));
	}
	@PutMapping("/items/{itemId}/complete")
	public ResponseEntity<LabOrderResponse> completeTest(
	        @PathVariable Long itemId,
	        @Valid @RequestBody LabTestResultRequest request) {

	    LabOrder order = labOrderService.completeTest(itemId, request);

	    return ResponseEntity.ok(responseMapper.toResponse(order));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<LabOrderResponse> cancelLabOrder(
	        @PathVariable Long orderId) {

	    LabOrder order = labOrderService.cancelLabOrder(orderId);

	    return ResponseEntity.ok(responseMapper.toResponse(order));
	}
}
