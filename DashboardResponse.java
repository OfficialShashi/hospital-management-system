package com.hms.dashboard.dto;

public class DashboardResponse {

    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long todayAppointments;
    private long totalPrescriptions;
    private long totalLabTests;
    private long totalMedicines;
    private long totalInvoices;
    private long totalPayments;

    public DashboardResponse() {
    }

    public DashboardResponse(
            long totalPatients,
            long totalDoctors,
            long totalAppointments,
            long todayAppointments,
            long totalPrescriptions,
            long totalLabTests,
            long totalMedicines,
            long totalInvoices,
            long totalPayments) {

        this.totalPatients = totalPatients;
        this.totalDoctors = totalDoctors;
        this.totalAppointments = totalAppointments;
        this.todayAppointments = todayAppointments;
        this.totalPrescriptions = totalPrescriptions;
        this.totalLabTests = totalLabTests;
        this.totalMedicines = totalMedicines;
        this.totalInvoices = totalInvoices;
        this.totalPayments = totalPayments;
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

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public long getTotalPrescriptions() {
        return totalPrescriptions;
    }

    public long getTotalLabTests() {
        return totalLabTests;
    }

    public long getTotalMedicines() {
        return totalMedicines;
    }

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public long getTotalPayments() {
        return totalPayments;
    }
}