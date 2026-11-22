
package com.hms.pharmacy.dto;

import org.springframework.stereotype.Component;

import com.hms.pharmacy.Medicine;

@Component
public class MedicineResponseMapper {

    public MedicineResponse toResponse(Medicine medicine) {

        MedicineResponse response = new MedicineResponse();

        response.setMedicineId(medicine.getMedicineId());
        response.setMedicineCode(medicine.getMedicineCode());
        response.setMedicineName(medicine.getMedicineName());
        response.setGenericName(medicine.getGenericName());
        response.setManufacturer(medicine.getManufacturer());
        response.setCategory(medicine.getCategory());
        response.setDosageForm(medicine.getDosageForm());
        response.setStrength(medicine.getStrength());
        response.setUnitPrice(medicine.getUnitPrice());
        response.setReorderLevel(medicine.getReorderLevel());
        response.setStatus(medicine.getStatus());
        response.setCreatedAt(medicine.getCreatedAt());
        response.setUpdatedAt(medicine.getUpdatedAt());

        return response;
    }
}
