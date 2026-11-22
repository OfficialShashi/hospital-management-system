package com.hms.report.dto;

import java.math.BigDecimal;

public class ReportResponse {

    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long totalInvoices;
    private BigDecimal totalRevenue;

    public ReportResponse() {
    }

    public ReportResponse(
            long totalPatients,
            long totalDoctors,
            long totalAppointments,
            long completedAppointments,
            long cancelledAppointments,
            long totalInvoices,
            BigDecimal totalRevenue) {

        this.totalPatients = totalPatients;
        this.totalDoctors = totalDoctors;
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.cancelledAppointments = cancelledAppointments;
        this.totalInvoices = totalInvoices;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}