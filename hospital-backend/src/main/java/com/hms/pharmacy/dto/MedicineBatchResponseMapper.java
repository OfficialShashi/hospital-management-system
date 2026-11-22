
package com.hms.pharmacy.dto;

import org.springframework.stereotype.Component;

import com.hms.pharmacy.MedicineBatch;

@Component
public class MedicineBatchResponseMapper {

    public MedicineBatchResponse toResponse(MedicineBatch batch) {

        MedicineBatchResponse response = new MedicineBatchResponse();

        response.setBatchId(batch.getBatchId());
        response.setMedicineId(batch.getMedicine().getMedicineId());
        response.setMedicineName(batch.getMedicine().getMedicineName());
        response.setMedicineCode(batch.getMedicine().getMedicineCode());
        response.setBatchNumber(batch.getBatchNumber());
        response.setQuantityReceived(batch.getQuantityReceived());
        response.setQuantityAvailable(batch.getQuantityAvailable());
        response.setManufacturingDate(batch.getManufacturingDate());
        response.setExpiryDate(batch.getExpiryDate());
        response.setPurchasePrice(batch.getPurchasePrice());
        response.setCreatedAt(batch.getCreatedAt());
        response.setUpdatedAt(batch.getUpdatedAt());

        return response;
    }
}
