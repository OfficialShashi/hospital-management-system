
package com.hms.pharmacy.dto;

import java.time.LocalDateTime;

public class MedicineDispensingResponse {

    private Long dispensingId;
    private Long prescriptionItemId;
    private String medicineName;
    private Long batchId;
    private String batchNumber;
    private Integer quantityDispensed;
    private Integer remainingStock;
    private Long pharmacistUserId;
    private String pharmacistName;
    private LocalDateTime dispensedAt;

    public Long getDispensingId() {
        return dispensingId;
    }

    public void setDispensingId(Long dispensingId) {
        this.dispensingId = dispensingId;
    }

    public Long getPrescriptionItemId() {
        return prescriptionItemId;
    }

    public void setPrescriptionItemId(Long prescriptionItemId) {
        this.prescriptionItemId = prescriptionItemId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getQuantityDispensed() {
        return quantityDispensed;
    }

    public void setQuantityDispensed(Integer quantityDispensed) {
        this.quantityDispensed = quantityDispensed;
    }

    public Integer getRemainingStock() {
        return remainingStock;
    }

    public void setRemainingStock(Integer remainingStock) {
        this.remainingStock = remainingStock;
    }

    public Long getPharmacistUserId() {
        return pharmacistUserId;
    }

    public void setPharmacistUserId(Long pharmacistUserId) {
        this.pharmacistUserId = pharmacistUserId;
    }

    public String getPharmacistName() {
        return pharmacistName;
    }

    public void setPharmacistName(String pharmacistName) {
        this.pharmacistName = pharmacistName;
    }

    public LocalDateTime getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(LocalDateTime dispensedAt) {
        this.dispensedAt = dispensedAt;
    }
}
