
package com.hms.appointment.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public class AppointmentRescheduleRequest {

    @NotNull(message = "New appointment date and time are required")
    @Future(message = "New appointment date and time must be in the future")
    private LocalDateTime appointmentDateTime;

    public AppointmentRescheduleRequest() {
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
}
