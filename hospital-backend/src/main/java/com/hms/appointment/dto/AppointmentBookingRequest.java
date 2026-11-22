
package com.hms.appointment.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AppointmentBookingRequest {

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Invalid patient ID")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Invalid doctor ID")
    private Long doctorId;

    @NotNull(message = "Appointment date and time are required")
    @Future(message = "Appointment date and time must be in the future")
    private LocalDateTime appointmentDateTime;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public AppointmentBookingRequest() {
    }

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

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
