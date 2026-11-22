
package com.hms.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class InvoiceRequest {

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be positive")
    private Long patientId;

    @Positive(message = "Appointment ID must be positive")
    private Long appointmentId;

    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    @Digits(integer = 10, fraction = 2,
            message = "Discount must have maximum 2 decimal places")
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.00", message = "Tax cannot be negative")
    @Digits(integer = 10, fraction = 2,
            message = "Tax must have maximum 2 decimal places")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    @NotEmpty(message = "Invoice must contain at least one item")
    @Valid
    private List<InvoiceItemRequest> items;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<InvoiceItemRequest> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemRequest> items) {
        this.items = items;
    }
}
