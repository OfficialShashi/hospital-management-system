
package com.hms.pharmacy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hms.pharmacy.MedicineBatch;

import jakarta.persistence.LockModeType;

@Repository
public interface MedicineBatchRepository
        extends JpaRepository<MedicineBatch, Long> {

    @Override
    @EntityGraph(attributePaths = {"medicine"})
    List<MedicineBatch> findAll();

    @Override
    @EntityGraph(attributePaths = {"medicine"})
    Optional<MedicineBatch> findById(Long id);

    @EntityGraph(attributePaths = {"medicine"})
    List<MedicineBatch> findByMedicine_MedicineId(Long medicineId);

    Optional<MedicineBatch> findByMedicine_MedicineIdAndBatchNumber(
            Long medicineId, String batchNumber);

    boolean existsByMedicine_MedicineIdAndBatchNumber(
            Long medicineId, String batchNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"medicine"})
    @Query("SELECT b FROM MedicineBatch b WHERE b.batchId = :batchId")
    Optional<MedicineBatch> findByIdForUpdate(
            @Param("batchId") Long batchId);
}
