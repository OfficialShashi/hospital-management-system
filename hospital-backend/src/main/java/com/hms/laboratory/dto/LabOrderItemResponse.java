
package com.hms.laboratory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hms.laboratory.LabOrder;

public class LabOrderItemResponse {

    private Long labOrderItemId;
    private Long testId;
    private String testCode;
    private String testName;
    private BigDecimal price;
    private LabOrder.OrderStatus status;
    private LocalDateTime sampleCollectedAt;
    private String resultValue;
    private String resultUnit;
    private String referenceRange;
    private String resultNotes;
    private LocalDateTime resultDate;

    public Long getLabOrderItemId() {
        return labOrderItemId;
    }

    public void setLabOrderItemId(Long labOrderItemId) {
        this.labOrderItemId = labOrderItemId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LabOrder.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(LabOrder.OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getSampleCollectedAt() {
        return sampleCollectedAt;
    }

    public void setSampleCollectedAt(LocalDateTime sampleCollectedAt) {
        this.sampleCollectedAt = sampleCollectedAt;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultUnit() {
        return resultUnit;
    }

    public void setResultUnit(String resultUnit) {
        this.resultUnit = resultUnit;
    }

    public String getReferenceRange() {
        return referenceRange;
    }

    public void setReferenceRange(String referenceRange) {
        this.referenceRange = referenceRange;
    }

    public String getResultNotes() {
        return resultNotes;
    }

    public void setResultNotes(String resultNotes) {
        this.resultNotes = resultNotes;
    }

    public LocalDateTime getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDateTime resultDate) {
        this.resultDate = resultDate;
    }
}
