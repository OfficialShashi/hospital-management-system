package com.hms.appointment.dto;

import java.time.LocalDateTime;

import com.hms.appointment.Appointment;

public class AppointmentResponseDTO {

    private Long appointmentId;
    private String appointmentNumber;
    private LocalDateTime appointmentDateTime;
    private Appointment.AppointmentStatus appointmentStatus;
    private String reason;
    private String cancellationReason;
    private String patientName;
    private String doctorName;
    private String departmentName;

    public AppointmentResponseDTO(Appointment appointment) {

        this.appointmentId = appointment.getAppointmentId();
        this.appointmentNumber = appointment.getAppointmentNumber();
        this.appointmentDateTime = appointment.getAppointmentDateTime();
        this.appointmentStatus = appointment.getAppointmentStatus();
        this.reason = appointment.getReason();
        this.cancellationReason = appointment.getCancellationReason();

        this.patientName = appointment.getPatient().getUser().getFullName();
        this.doctorName = appointment.getDoctor().getUser().getFullName();
        this.departmentName = appointment.getDepartment().getDepartmentName();
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public Appointment.AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public String getReason() {
        return reason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDepartmentName() {
        return departmentName;
    }
}