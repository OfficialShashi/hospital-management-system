
package com.hms.pharmacy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.pharmacy.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByMedicineCode(String medicineCode);

    boolean existsByMedicineCode(String medicineCode);

    List<Medicine> findByStatus(Medicine.MedicineStatus status);

    List<Medicine> findByCategoryIgnoreCase(String category);

    List<Medicine> findByMedicineNameContainingIgnoreCase(String medicineName);

}

