
package com.hms.laboratory.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.hms.laboratory.LabOrder;

public class LabOrderResponse {

    private Long labOrderId;
    private String orderNumber;

    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String doctorName;

    private Long appointmentId;

    private LocalDateTime orderDate;
    private LabOrder.Priority priority;
    private LabOrder.OrderStatus status;

    private String clinicalNotes;

    private List<LabOrderItemResponse> items;

    public Long getLabOrderId() {
        return labOrderId;
    }

    public void setLabOrderId(Long labOrderId) {
        this.labOrderId = labOrderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LabOrder.Priority getPriority() {
        return priority;
    }

    public void setPriority(LabOrder.Priority priority) {
        this.priority = priority;
    }

    public LabOrder.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(LabOrder.OrderStatus status) {
        this.status = status;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public List<LabOrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<LabOrderItemResponse> items) {
        this.items = items;
    }
}
