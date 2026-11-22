
package com.hms.billing.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.hms.billing.Invoice;
import com.hms.billing.dto.InvoiceRequest;
import com.hms.billing.dto.InvoiceResponse;
import com.hms.billing.service.InvoiceService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invoices")
@PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {

        InvoiceResponse response = invoiceService.createInvoice(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {

        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(
            @PathVariable String invoiceNumber) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceByNumber(invoiceNumber));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByPatient(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(
                invoiceService.getInvoicesByPatient(patientId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStatus(
            @PathVariable Invoice.InvoiceStatus status) {

        return ResponseEntity.ok(
                invoiceService.getInvoicesByStatus(status));
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable Long id,
            @PathVariable Invoice.InvoiceStatus status) {

        return ResponseEntity.ok(
                invoiceService.updateInvoiceStatus(id, status));
    }
}
