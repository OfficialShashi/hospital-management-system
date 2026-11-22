
package com.hms.pharmacy;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.hms.prescription.PrescriptionItem;
import com.hms.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicine_dispensing")
public class MedicineDispensing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispensing_id")
    private Long dispensingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_item_id", nullable = false)
    private PrescriptionItem prescriptionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private MedicineBatch batch;

    @Column(name = "quantity_dispensed", nullable = false)
    private Integer quantityDispensed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensed_by", nullable = false)
    private User dispensedBy;

    @CreationTimestamp
    @Column(name = "dispensed_at", updatable = false)
    private LocalDateTime dispensedAt;

    public MedicineDispensing() {
    }

    public Long getDispensingId() {
        return dispensingId;
    }

    public void setDispensingId(Long dispensingId) {
        this.dispensingId = dispensingId;
    }

    public PrescriptionItem getPrescriptionItem() {
        return prescriptionItem;
    }

    public void setPrescriptionItem(PrescriptionItem prescriptionItem) {
        this.prescriptionItem = prescriptionItem;
    }

    public MedicineBatch getBatch() {
        return batch;
    }

    public void setBatch(MedicineBatch batch) {
        this.batch = batch;
    }

    public Integer getQuantityDispensed() {
        return quantityDispensed;
    }

    public void setQuantityDispensed(Integer quantityDispensed) {
        this.quantityDispensed = quantityDispensed;
    }

    public User getDispensedBy() {
        return dispensedBy;
    }

    public void setDispensedBy(User dispensedBy) {
        this.dispensedBy = dispensedBy;
    }

    public LocalDateTime getDispensedAt() {
        return dispensedAt;
    }
}
