
package com.hms.laboratory.dto;

import java.util.List;

import com.hms.laboratory.LabOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class LabOrderRequest {

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be positive")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be positive")
    private Long doctorId;

    @Positive(message = "Appointment ID must be positive")
    private Long appointmentId;

    @NotNull(message = "Priority is required")
    private LabOrder.Priority priority = LabOrder.Priority.ROUTINE;

    @Size(max = 2000)
    private String clinicalNotes;

    @NotEmpty(message = "At least one lab test is required")
    @Valid
    private List<LabOrderItemRequest> items;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LabOrder.Priority getPriority() {
        return priority;
    }

    public void setPriority(LabOrder.Priority priority) {
        this.priority = priority;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public List<LabOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<LabOrderItemRequest> items) {
        this.items = items;
    }
}
