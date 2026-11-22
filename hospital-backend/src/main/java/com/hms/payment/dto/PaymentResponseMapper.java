
package com.hms.payment.dto;

import org.springframework.stereotype.Component;

import com.hms.payment.Payment;

@Component
public class PaymentResponseMapper {

    public PaymentResponse toResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setPaymentId(payment.getPaymentId());
        response.setPaymentNumber(payment.getPaymentNumber());

        response.setInvoiceId(
                payment.getInvoice().getInvoiceId());

        response.setInvoiceNumber(
                payment.getInvoice().getInvoiceNumber());

        response.setPatientId(
                payment.getInvoice().getPatient().getPatientId());

        response.setPatientName(
                payment.getInvoice().getPatient()
                        .getUser().getFullName());

        response.setPaymentDate(payment.getPaymentDate());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());

        response.setTransactionReference(
                payment.getTransactionReference());

        response.setNotes(payment.getNotes());

        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }
}
