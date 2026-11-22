
package com.hms.laboratory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hms.laboratory.LabTest;
import com.hms.laboratory.repository.LabTestRepository;

@Service
public class LabTestService {

    private final LabTestRepository labTestRepository;

    public LabTestService(LabTestRepository labTestRepository) {
        this.labTestRepository = labTestRepository;
    }

    public List<LabTest> getAllTests() {
        return labTestRepository.findAll();
    }

    public LabTest getTestById(Long testId) {
        return labTestRepository.findById(testId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab test not found"));
    }

    public LabTest getTestByCode(String testCode) {
        return labTestRepository.findByTestCode(testCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab test not found"));
    }

    public List<LabTest> getTestsByStatus(LabTest.TestStatus status) {
        return labTestRepository.findByStatus(status);
    }

    public List<LabTest> getTestsByCategory(String category) {
        return labTestRepository.findByCategoryIgnoreCase(category);
    }

    public List<LabTest> searchTests(String testName) {
        return labTestRepository.findByTestNameContainingIgnoreCase(testName);
    }

    public LabTest createTest(LabTest labTest) {

        if (labTestRepository.existsByTestCode(labTest.getTestCode())) {
            throw new IllegalArgumentException("Test code already exists");
        }

        if (labTest.getPrice() == null
                || labTest.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Invalid test price");
        }

        labTest.setStatus(LabTest.TestStatus.ACTIVE);

        return labTestRepository.save(labTest);
    }

    public LabTest updateTest(Long testId, LabTest updatedTest) {

        LabTest existingTest = getTestById(testId);

        existingTest.setTestName(updatedTest.getTestName());
        existingTest.setCategory(updatedTest.getCategory());
        existingTest.setDescription(updatedTest.getDescription());
        existingTest.setPrice(updatedTest.getPrice());
        existingTest.setSampleType(updatedTest.getSampleType());
        existingTest.setNormalRange(updatedTest.getNormalRange());
        existingTest.setUnit(updatedTest.getUnit());
        existingTest.setPreparationInstructions(
                updatedTest.getPreparationInstructions());

        return labTestRepository.save(existingTest);
    }

    public LabTest updateTestStatus(
            Long testId, LabTest.TestStatus status) {

        LabTest existingTest = getTestById(testId);

        existingTest.setStatus(status);

        return labTestRepository.save(existingTest);
    }
}
