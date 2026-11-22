
package com.hms.billing.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hms.billing.Invoice;
import com.hms.billing.InvoiceItem;
import com.hms.billing.repository.InvoiceItemRepository;
import com.hms.payment.Payment.PaymentStatus;
import com.hms.payment.repository.PaymentRepository;

@Component
public class InvoiceResponseMapper {

    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceItemResponseMapper itemMapper;
    private final PaymentRepository paymentRepository;

    public InvoiceResponseMapper(
            InvoiceItemRepository invoiceItemRepository,
            InvoiceItemResponseMapper itemMapper,
            PaymentRepository paymentRepository) {

        this.invoiceItemRepository = invoiceItemRepository;
        this.itemMapper = itemMapper;
        this.paymentRepository = paymentRepository;
    }

    public InvoiceResponse toResponse(Invoice invoice) {

        InvoiceResponse response = new InvoiceResponse();

        response.setInvoiceId(invoice.getInvoiceId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());

        response.setPatientId(
                invoice.getPatient().getPatientId());

        response.setPatientName(
                invoice.getPatient().getUser().getFullName());

        response.setAppointmentId(invoice.getAppointmentId());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setSubtotal(invoice.getSubtotal());
        response.setDiscount(invoice.getDiscount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setInvoiceStatus(invoice.getInvoiceStatus());
        response.setDueDate(invoice.getDueDate());
        response.setNotes(invoice.getNotes());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());

        BigDecimal paidAmount =
                paymentRepository.getTotalAmountByInvoiceAndStatus(
                        invoice.getInvoiceId(),
                        PaymentStatus.SUCCESS);

        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }

        BigDecimal balanceAmount =
                invoice.getTotalAmount().subtract(paidAmount);

        if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            balanceAmount = BigDecimal.ZERO;
        }

        response.setPaidAmount(paidAmount);
        response.setBalanceAmount(balanceAmount);

        List<InvoiceItem> items =
                invoiceItemRepository.findAllByInvoice_InvoiceId(
                        invoice.getInvoiceId());

        response.setItems(
                items.stream()
                        .map(itemMapper::toResponse)
                        .toList());

        return response;
    }
}
