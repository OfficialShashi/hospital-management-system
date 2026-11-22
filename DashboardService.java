package com.hms.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.dashboard.dto.DashboardResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.hms.patient.repository.PatientRepository;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.laboratory.repository.LabOrderRepository;
import com.hms.pharmacy.repository.MedicineRepository;
import com.hms.billing.repository.InvoiceRepository;
import com.hms.payment.repository.PaymentRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabOrderRepository labOrderRepository;
    private final MedicineRepository medicineRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            PrescriptionRepository prescriptionRepository,
            LabOrderRepository labOrderRepository,
            MedicineRepository medicineRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository) {

        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labOrderRepository = labOrderRepository;
        this.medicineRepository = medicineRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }
    
    public DashboardResponse getDashboard() {

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();

        long todayAppointments = appointmentRepository
                .findByAppointmentDateTimeBetween(
                        startOfDay, endOfDay)
                .size();

        long totalPrescriptions = prescriptionRepository.count();
        long totalLabTests = labOrderRepository.count();
        long totalMedicines = medicineRepository.count();
        long totalInvoices = invoiceRepository.count();
        long totalPayments = paymentRepository.count();

        return new DashboardResponse(
                totalPatients,
                totalDoctors,
                totalAppointments,
                todayAppointments,
                totalPrescriptions,
                totalLabTests,
                totalMedicines,
                totalInvoices,
                totalPayments);
    }
}