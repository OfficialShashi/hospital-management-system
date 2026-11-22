
package com.hms.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.hms.billing.Invoice;
import com.hms.billing.repository.InvoiceRepository;
import com.hms.payment.Payment;
import com.hms.payment.Payment.PaymentStatus;
import com.hms.payment.dto.PaymentRequest;
import com.hms.payment.dto.PaymentResponse;
import com.hms.payment.dto.PaymentResponseMapper;
import com.hms.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentResponseMapper responseMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            PaymentResponseMapper responseMapper) {

        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.responseMapper = responseMapper;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResponse createPayment(PaymentRequest request) {

        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
        }

        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException(
                    "Payment method is required");
        }

        BigDecimal amount = request.getAmount().setScale(2,
                java.math.RoundingMode.UNNECESSARY);

        Invoice invoice = invoiceRepository.findByIdForUpdate(
                request.getInvoiceId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        validateInvoiceAcceptsPayment(invoice);

        BigDecimal committedAmount = getCommittedAmount(
                invoice.getInvoiceId());

        BigDecimal remainingAmount = invoice.getTotalAmount()
                .subtract(committedAmount);

        if (amount.compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment exceeds remaining invoice balance: "
                            + remainingAmount);
        }

        Payment payment = new Payment();

        payment.setPaymentNumber(generatePaymentNumber());
        payment.setInvoice(invoice);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(amount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionReference(
                request.getTransactionReference());
        payment.setNotes(request.getNotes());

        Payment savedPayment = paymentRepository.save(payment);

        return responseMapper.toResponse(savedPayment);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResponse confirmPayment(Long paymentId) {

        Payment existingPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        Long invoiceId = existingPayment.getInvoice().getInvoiceId();

        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING payments can be confirmed");
        }

        validateInvoiceAcceptsPayment(invoice);

        BigDecimal alreadyPaid = getSuccessfulAmount(invoiceId);

        BigDecimal remainingAmount = invoice.getTotalAmount()
                .subtract(alreadyPaid);

        if (payment.getAmount().compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment exceeds remaining invoice balance: "
                            + remainingAmount);
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal totalPaid = alreadyPaid.add(payment.getAmount());

        updateInvoicePaymentStatus(invoice, totalPaid);

        invoiceRepository.save(invoice);

        return responseMapper.toResponse(savedPayment);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResponse failPayment(Long paymentId) {

        Payment existingPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        Long invoiceId = existingPayment.getInvoice().getInvoiceId();

        invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING payments can be marked as FAILED");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);

        Payment savedPayment = paymentRepository.save(payment);

        return responseMapper.toResponse(savedPayment);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResponse refundPayment(Long paymentId) {

        Payment existingPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        Long invoiceId = existingPayment.getInvoice().getInvoiceId();

        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException(
                    "Only SUCCESS payments can be refunded");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal remainingPaid = getSuccessfulAmount(invoiceId);

        updateInvoicePaymentStatus(invoice, remainingPaid);

        invoiceRepository.save(invoice);

        return responseMapper.toResponse(savedPayment);
    }

    private BigDecimal getCommittedAmount(Long invoiceId) {

        return 	paymentRepository.findByInvoice_InvoiceIdOrderByPaymentDateDesc(invoiceId)

                .stream()
                .filter(payment ->
                        payment.getPaymentStatus() == PaymentStatus.SUCCESS
                        || payment.getPaymentStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getSuccessfulAmount(Long invoiceId) {

        return paymentRepository.findByInvoice_InvoiceId(invoiceId)
                .stream()
                .filter(payment ->
                        payment.getPaymentStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateInvoiceAcceptsPayment(Invoice invoice) {

        if (invoice.getInvoiceStatus() != Invoice.InvoiceStatus.ISSUED
                && invoice.getInvoiceStatus()
                        != Invoice.InvoiceStatus.PARTIALLY_PAID) {

            throw new IllegalArgumentException(
                    "Payment is allowed only for ISSUED or PARTIALLY_PAID invoices");
        }
    }

    private void updateInvoicePaymentStatus(
            Invoice invoice,
            BigDecimal totalPaid) {

        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setInvoiceStatus(Invoice.InvoiceStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setInvoiceStatus(
                    Invoice.InvoiceStatus.PARTIALLY_PAID);
        } else {
            invoice.setInvoiceStatus(Invoice.InvoiceStatus.ISSUED);
        }
    }

    private String generatePaymentNumber() {

        String paymentNumber;

        do {
            paymentNumber = "PAY-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

        } while (paymentRepository.existsByPaymentNumber(paymentNumber));

        return paymentNumber;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        return responseMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByNumber(String paymentNumber) {

        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        return responseMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {

        return paymentRepository.findByInvoice_InvoiceId(invoiceId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(
            PaymentStatus status) {

        return paymentRepository.findByPaymentStatus(status)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }
}
