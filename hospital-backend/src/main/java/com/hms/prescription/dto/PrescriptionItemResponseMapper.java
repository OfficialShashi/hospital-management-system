
package com.hms.prescription.dto;

import org.springframework.stereotype.Component;

import com.hms.prescription.PrescriptionItem;

@Component
public class PrescriptionItemResponseMapper {

    public PrescriptionItemResponse toResponse(PrescriptionItem item) {

        PrescriptionItemResponse response =
                new PrescriptionItemResponse();

        response.setItemId(item.getItemId());

        response.setPrescriptionId(
                item.getPrescription().getPrescriptionId());

        response.setMedicineName(item.getMedicineName());
        response.setDosage(item.getDosage());
        response.setRoute(item.getRoute());
        response.setFrequency(item.getFrequency());
        response.setDurationDays(item.getDurationDays());
        response.setQuantity(item.getQuantity());
        response.setInstructions(item.getInstructions());
        response.setCreatedAt(item.getCreatedAt());

        return response;
    }
}
