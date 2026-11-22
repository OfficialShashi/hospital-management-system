
package com.hms.pharmacy.dto;

import org.springframework.stereotype.Component;

import com.hms.pharmacy.MedicineDispensing;

@Component
public class MedicineDispensingResponseMapper {

    public MedicineDispensingResponse toResponse(
            MedicineDispensing dispensing) {

        MedicineDispensingResponse response =
                new MedicineDispensingResponse();

        response.setDispensingId(dispensing.getDispensingId());

        response.setPrescriptionItemId(
                dispensing.getPrescriptionItem().getItemId());

        response.setMedicineName(
                dispensing.getPrescriptionItem().getMedicineName());

        response.setBatchId(
                dispensing.getBatch().getBatchId());

        response.setBatchNumber(
                dispensing.getBatch().getBatchNumber());

        response.setQuantityDispensed(
                dispensing.getQuantityDispensed());

        response.setRemainingStock(
                dispensing.getBatch().getQuantityAvailable());

        response.setPharmacistUserId(
                dispensing.getDispensedBy().getUserId());

        response.setPharmacistName(
                dispensing.getDispensedBy().getFullName());

        response.setDispensedAt(dispensing.getDispensedAt());

        return response;
    }
}
