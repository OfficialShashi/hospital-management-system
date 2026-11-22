package com.hms.report.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hms.payment.repository.PaymentRepository;
import com.hms.payment.Payment.PaymentStatus;
import com.hms.report.dto.ReportResponse;
import com.hms.patient.repository.PatientRepository;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.appointment.Appointment;
import com.hms.billing.repository.InvoiceRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public ReportService(
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            InvoiceRepository invoiceRepository, PaymentRepository paymentRepository) {

        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    public ReportResponse getSummaryReport() {

        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();

        long completedAppointments = appointmentRepository
                .countByAppointmentStatus(
                        Appointment.AppointmentStatus.COMPLETED);

        long cancelledAppointments = appointmentRepository
                .countByAppointmentStatus(
                        Appointment.AppointmentStatus.CANCELLED);

        long totalInvoices = invoiceRepository.count();

        BigDecimal totalRevenue =
                paymentRepository.getTotalAmountByPaymentStatus(
                        PaymentStatus.SUCCESS);

        return new ReportResponse(
                totalPatients,
                totalDoctors,
                totalAppointments,
                completedAppointments,
                cancelledAppointments,
                totalInvoices,
                totalRevenue);
    }
}