
package com.hms.laboratory.dto;

import org.springframework.stereotype.Component;

import com.hms.laboratory.LabTest;

@Component
public class LabTestResponseMapper {

    public LabTestResponse toResponse(LabTest labTest) {

        LabTestResponse response = new LabTestResponse();

        response.setTestId(labTest.getTestId());
        response.setTestCode(labTest.getTestCode());
        response.setTestName(labTest.getTestName());
        response.setCategory(labTest.getCategory());
        response.setDescription(labTest.getDescription());
        response.setPrice(labTest.getPrice());
        response.setSampleType(labTest.getSampleType());
        response.setNormalRange(labTest.getNormalRange());
        response.setUnit(labTest.getUnit());

        response.setPreparationInstructions(
                labTest.getPreparationInstructions());

        response.setStatus(labTest.getStatus());
        response.setCreatedAt(labTest.getCreatedAt());
        response.setUpdatedAt(labTest.getUpdatedAt());

        return response;
    }
}
