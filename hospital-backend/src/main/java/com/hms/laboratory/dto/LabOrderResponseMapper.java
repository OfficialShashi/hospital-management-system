
package com.hms.laboratory.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hms.laboratory.LabOrder;
import com.hms.laboratory.LabOrderItem;

@Component
public class LabOrderResponseMapper {

    public LabOrderResponse toResponse(LabOrder order) {

        LabOrderResponse response = new LabOrderResponse();

        response.setLabOrderId(order.getLabOrderId());
        response.setOrderNumber(order.getOrderNumber());

        response.setPatientId(order.getPatient().getPatientId());
        response.setPatientName(
                order.getPatient().getUser().getFullName());

        response.setDoctorId(order.getDoctor().getDoctorId());
        response.setDoctorName(
                order.getDoctor().getUser().getFullName());

        response.setAppointmentId(
                order.getAppointment() != null
                        ? order.getAppointment().getAppointmentId()
                        : null);

        response.setOrderDate(order.getOrderDate());
        response.setPriority(order.getPriority());
        response.setStatus(order.getStatus());
        response.setClinicalNotes(order.getClinicalNotes());

        List<LabOrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        response.setItems(itemResponses);

        return response;
    }

    private LabOrderItemResponse toItemResponse(LabOrderItem item) {

        LabOrderItemResponse response = new LabOrderItemResponse();

        response.setLabOrderItemId(item.getLabOrderItemId());

        response.setTestId(item.getLabTest().getTestId());
        response.setTestCode(item.getLabTest().getTestCode());
        response.setTestName(item.getLabTest().getTestName());

        response.setPrice(item.getPrice());
        response.setStatus(item.getStatus());

        response.setSampleCollectedAt(item.getSampleCollectedAt());
        response.setResultValue(item.getResultValue());
        response.setResultUnit(item.getResultUnit());
        response.setReferenceRange(item.getReferenceRange());
        response.setResultNotes(item.getResultNotes());
        response.setResultDate(item.getResultDate());

        return response;
    }
}
