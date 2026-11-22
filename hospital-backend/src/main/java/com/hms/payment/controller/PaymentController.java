
package com.hms.payment.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hms.payment.Payment.PaymentStatus;
import com.hms.payment.dto.PaymentRequest;
import com.hms.payment.dto.PaymentResponse;
import com.hms.payment.service.PaymentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response =
                paymentService.createPayment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.confirmPayment(id));
    }


    @PutMapping("/{id}/fail")
    public ResponseEntity<PaymentResponse> failPayment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.failPayment(id));
    }


    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(id));
    }

    @GetMapping("/number/{paymentNumber}")
    public ResponseEntity<PaymentResponse> getPaymentByNumber(
            @PathVariable String paymentNumber) {

        return ResponseEntity.ok(
                paymentService.getPaymentByNumber(paymentNumber));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoice(
            @PathVariable Long invoiceId) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByInvoice(invoiceId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @PathVariable PaymentStatus status) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(status));
    }
}
