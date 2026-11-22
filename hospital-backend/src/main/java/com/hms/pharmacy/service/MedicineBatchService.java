
package com.hms.pharmacy.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.pharmacy.Medicine;
import com.hms.pharmacy.MedicineBatch;
import com.hms.pharmacy.repository.MedicineBatchRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.pharmacy.dto.MedicineBatchRequest;

@Service
public class MedicineBatchService {

    private final MedicineBatchRepository batchRepository;
    private final MedicineRepository medicineRepository;

    public MedicineBatchService(
            MedicineBatchRepository batchRepository,
            MedicineRepository medicineRepository) {
        this.batchRepository = batchRepository;
        this.medicineRepository = medicineRepository;
    }

    public List<MedicineBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    public MedicineBatch getBatchById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Medicine batch not found: " + id));
    }

    public List<MedicineBatch> getBatchesByMedicine(Long medicineId) {
        if (!medicineRepository.existsById(medicineId)) {
            throw new IllegalArgumentException("Medicine not found: " + medicineId);
        }

        return batchRepository.findByMedicine_MedicineId(medicineId);
    }

    @Transactional
    public MedicineBatch createBatch(MedicineBatchRequest request) {

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Medicine not found: " + request.getMedicineId()));

        if (medicine.getStatus() != Medicine.MedicineStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Cannot add stock for an inactive medicine");
        }

        if (batchRepository.existsByMedicine_MedicineIdAndBatchNumber(
                request.getMedicineId(), request.getBatchNumber())) {
            throw new IllegalArgumentException("Batch number already exists for this medicine");
        }

        LocalDate today = LocalDate.now();

        if (request.getExpiryDate().isBefore(today)) {
            throw new IllegalArgumentException("Expired medicine batch cannot be added");
        }

        if (request.getManufacturingDate() != null) {

            if (request.getManufacturingDate().isAfter(today)) {
                throw new IllegalArgumentException(
                        "Manufacturing date cannot be in the future");
            }

            if (!request.getManufacturingDate().isBefore(request.getExpiryDate())) {
                throw new IllegalArgumentException(
                        "Manufacturing date must be before expiry date");
            }
        }

        MedicineBatch batch = new MedicineBatch();

        batch.setMedicine(medicine);
        batch.setBatchNumber(request.getBatchNumber());
        batch.setQuantityReceived(request.getQuantityReceived());
        batch.setQuantityAvailable(request.getQuantityReceived());
        batch.setManufacturingDate(request.getManufacturingDate());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setPurchasePrice(request.getPurchasePrice());

        return batchRepository.save(batch);
    }
}
