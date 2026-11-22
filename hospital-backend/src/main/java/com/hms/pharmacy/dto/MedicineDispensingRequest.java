
package com.hms.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MedicineDispensingRequest {

    @NotNull(message = "Prescription item ID is required")
    @Positive(message = "Prescription item ID must be positive")
    private Long prescriptionItemId;

    @NotNull(message = "Batch ID is required")
    @Positive(message = "Batch ID must be positive")
    private Long batchId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Dispensing quantity must be positive")
    private Integer quantity;

    public Long getPrescriptionItemId() {
        return prescriptionItemId;
    }

    public void setPrescriptionItemId(Long prescriptionItemId) {
        this.prescriptionItemId = prescriptionItemId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
