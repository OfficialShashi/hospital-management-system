
package com.hms.pharmacy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hms.pharmacy.MedicineDispensing;

@Repository
public interface MedicineDispensingRepository
        extends JpaRepository<MedicineDispensing, Long> {

    @EntityGraph(attributePaths = {
        "prescriptionItem",
        "prescriptionItem.prescription",
        "batch",
        "batch.medicine",
        "dispensedBy"
    })
    List<MedicineDispensing> findByPrescriptionItem_ItemId(Long itemId);

    @Query("""
        SELECT COALESCE(SUM(d.quantityDispensed), 0)
        FROM MedicineDispensing d
        WHERE d.prescriptionItem.itemId = :itemId
        """)
    Long getTotalDispensedQuantity(@Param("itemId") Long itemId);
}
