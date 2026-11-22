
package com.hms.laboratory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.laboratory.LabTest;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    Optional<LabTest> findByTestCode(String testCode);

    boolean existsByTestCode(String testCode);

    List<LabTest> findByStatus(LabTest.TestStatus status);

    List<LabTest> findByCategoryIgnoreCase(String category);

    List<LabTest> findByTestNameContainingIgnoreCase(String testName);
}
