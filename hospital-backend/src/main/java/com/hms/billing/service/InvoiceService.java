
package com.hms.billing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.appointment.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.billing.Invoice;
import com.hms.billing.InvoiceItem;
import com.hms.billing.dto.InvoiceItemRequest;
import com.hms.billing.dto.InvoiceRequest;
import com.hms.billing.dto.InvoiceResponse;
import com.hms.billing.dto.InvoiceResponseMapper;
import com.hms.billing.repository.InvoiceItemRepository;
import com.hms.billing.repository.InvoiceRepository;
import com.hms.patient.Patient;
import com.hms.patient.repository.PatientRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceResponseMapper responseMapper;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            InvoiceResponseMapper responseMapper) {

        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.responseMapper = responseMapper;
    }

    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        if (request.getAppointmentId() != null) {

            Appointment appointment = appointmentRepository
                    .findById(request.getAppointmentId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Appointment not found"));

            if (!appointment.getPatient().getPatientId()
                    .equals(patient.getPatientId())) {

                throw new IllegalArgumentException(
                        "Appointment does not belong to this patient");
            }
        }

        BigDecimal discount = request.getDiscount();
        BigDecimal taxAmount = request.getTaxAmount();

        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Discount cannot be null or negative");
        }

        if (taxAmount == null || taxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Tax amount cannot be null or negative");
        }

        Invoice invoice = new Invoice();

        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setPatient(patient);
        invoice.setAppointmentId(request.getAppointmentId());
        invoice.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxAmount(taxAmount.setScale(2, RoundingMode.HALF_UP));
        invoice.setDueDate(request.getDueDate());
        invoice.setNotes(request.getNotes());
        invoice.setInvoiceStatus(Invoice.InvoiceStatus.DRAFT);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (InvoiceItemRequest itemRequest : request.getItems()) {

            if (itemRequest.getUnitPrice() == null
                    || itemRequest.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "Unit price must be greater than zero");
            }

            if (itemRequest.getQuantity() == null
                    || itemRequest.getQuantity() <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than zero");
            }

            BigDecimal unitPrice = itemRequest.getUnitPrice()
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            InvoiceItem item = new InvoiceItem();

            item.setInvoice(savedInvoice);
            item.setItemType(itemRequest.getItemType());
            item.setDescription(itemRequest.getDescription());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);
            item.setSourceId(itemRequest.getSourceId());

            invoiceItemRepository.save(item);

            subtotal = subtotal.add(lineTotal);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "Discount cannot exceed subtotal");
        }

        BigDecimal total = subtotal.subtract(discount)
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Invoice total cannot be negative");
        }

        savedInvoice.setSubtotal(subtotal);
        savedInvoice.setTotalAmount(total);

        invoiceRepository.save(savedInvoice);

        return responseMapper.toResponse(savedInvoice);
    }

    private String generateInvoiceNumber() {

        String invoiceNumber;

        do {
            invoiceNumber = "INV-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

        } while (invoiceRepository.existsByInvoiceNumber(invoiceNumber));

        return invoiceNumber;
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {

        return invoiceRepository.findAll()
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        return responseMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {

        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        return responseMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByPatient(Long patientId) {

        return invoiceRepository.findByPatient_PatientId(patientId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(
            Invoice.InvoiceStatus status) {

        return invoiceRepository.findByInvoiceStatus(status)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional
    public InvoiceResponse updateInvoiceStatus(
            Long invoiceId,
            Invoice.InvoiceStatus newStatus) {

        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        if (invoice.getInvoiceStatus() != Invoice.InvoiceStatus.DRAFT) {

            throw new IllegalArgumentException(
                    "Only DRAFT invoices can be updated directly");
        }

        if (newStatus != Invoice.InvoiceStatus.ISSUED
                && newStatus != Invoice.InvoiceStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Only DRAFT to ISSUED or DRAFT to CANCELLED transitions are allowed");
        }

        invoice.setInvoiceStatus(newStatus);

        invoiceRepository.save(invoice);

        return responseMapper.toResponse(invoice);
    }
}
