
package com.hms.pharmacy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.pharmacy.Medicine;
import com.hms.pharmacy.Medicine.MedicineStatus;
import com.hms.pharmacy.dto.MedicineRequest;
import com.hms.pharmacy.repository.MedicineRepository;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Medicine not found with ID: " + id));
    }

    public Medicine getMedicineByCode(String code) {
        return medicineRepository.findByMedicineCode(code)
                .orElseThrow(() ->
                    new IllegalArgumentException("Medicine not found with code: " + code));
    }

    public List<Medicine> getMedicinesByStatus(MedicineStatus status) {
        return medicineRepository.findByStatus(status);
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategoryIgnoreCase(category);
    }

    public List<Medicine> searchMedicines(String name) {
        return medicineRepository.findByMedicineNameContainingIgnoreCase(name);
    }

    @Transactional
    public Medicine createMedicine(MedicineRequest request) {

        if (medicineRepository.existsByMedicineCode(request.getMedicineCode())) {
            throw new IllegalArgumentException("Medicine code already exists");
        }

        Medicine medicine = new Medicine();

        medicine.setMedicineCode(request.getMedicineCode());
        medicine.setMedicineName(request.getMedicineName());
        medicine.setGenericName(request.getGenericName());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setCategory(request.getCategory());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setStrength(request.getStrength());
        medicine.setUnitPrice(request.getUnitPrice());
        medicine.setReorderLevel(request.getReorderLevel());
        medicine.setStatus(MedicineStatus.ACTIVE);

        return medicineRepository.save(medicine);
    }

    @Transactional
    public Medicine updateMedicine(Long id, MedicineRequest request) {

        Medicine medicine = getMedicineById(id);

        medicineRepository.findByMedicineCode(request.getMedicineCode())
                .filter(existing -> !existing.getMedicineId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Medicine code already exists");
                });

        medicine.setMedicineCode(request.getMedicineCode());
        medicine.setMedicineName(request.getMedicineName());
        medicine.setGenericName(request.getGenericName());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setCategory(request.getCategory());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setStrength(request.getStrength());
        medicine.setUnitPrice(request.getUnitPrice());
        medicine.setReorderLevel(request.getReorderLevel());

        return medicineRepository.save(medicine);
    }

    @Transactional
    public Medicine updateMedicineStatus(Long id, MedicineStatus status) {

        Medicine medicine = getMedicineById(id);
        medicine.setStatus(status);

        return medicineRepository.save(medicine);
    }
}
