
package com.hms.prescription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hms.prescription.PrescriptionItem;

import jakarta.persistence.LockModeType;

@Repository
public interface PrescriptionItemRepository
        extends JpaRepository<PrescriptionItem, Long> {

    @EntityGraph(attributePaths = {
        "prescription",
        "prescription.patient",
        "prescription.patient.user",
        "prescription.doctor",
        "prescription.doctor.user",
        "prescription.doctor.department",
        "prescription.appointment"
    })
    List<PrescriptionItem> findByPrescription_PrescriptionId(
            Long prescriptionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT pi FROM PrescriptionItem pi
        WHERE pi.itemId = :itemId
        """)
    Optional<PrescriptionItem> findByIdForUpdate(
            @Param("itemId") Long itemId);
}
