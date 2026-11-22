
package com.hms.laboratory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class LabOrderItemRequest {

    @NotNull(message = "Test ID is required")
    @Positive(message = "Test ID must be positive")
    private Long testId;

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

}
