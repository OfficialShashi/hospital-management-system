
package com.hms.prescription.service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.appointment.Appointment;
import com.hms.appointment.Appointment.AppointmentStatus;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.doctor.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.prescription.Prescription;
import com.hms.prescription.PrescriptionItem;
import com.hms.prescription.PrescriptionStatus;
import com.hms.prescription.dto.PrescriptionResponse;
import com.hms.prescription.dto.PrescriptionResponseMapper;
import com.hms.prescription.repository.PrescriptionItemRepository;
import com.hms.prescription.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository itemRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionResponseMapper responseMapper;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            PrescriptionItemRepository itemRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            PrescriptionResponseMapper responseMapper) {

        this.prescriptionRepository = prescriptionRepository;
        this.itemRepository = itemRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.responseMapper = responseMapper;
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Prescription not found"));
    }

    public Prescription getPrescriptionByNumber(String number) {
        return prescriptionRepository.findByPrescriptionNumber(number)
                .orElseThrow(() ->
                        new IllegalArgumentException("Prescription not found"));
    }

    public List<Prescription> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatient_PatientId(patientId);
    }

    public List<Prescription> getPrescriptionsByDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctor_DoctorId(doctorId);
    }

    public List<PrescriptionItem> getPrescriptionItems(Long prescriptionId) {

        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new IllegalArgumentException("Prescription not found");
        }

        return itemRepository.findByPrescription_PrescriptionId(prescriptionId);
    }

    @Transactional
    public Prescription createPrescription(
            Long patientId,
            Long doctorId,
            Long appointmentId,
            String diagnosis,
            String instructions,
            List<PrescriptionItem> items) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one medicine item is required");
        }

        Appointment appointment = null;

        if (appointmentId != null) {
            appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Appointment not found"));

            if (!appointment.getPatient().getPatientId().equals(patientId)
                    || !appointment.getDoctor().getDoctorId().equals(doctorId)) {
                throw new IllegalArgumentException(
                        "Appointment does not belong to this patient and doctor");
            }

            if (appointment.getAppointmentStatus() != AppointmentStatus.COMPLETED) {
                throw new IllegalArgumentException(
                        "Prescription can only be linked to a completed appointment");
            }
        }

        Prescription prescription = new Prescription();

        prescription.setPrescriptionNumber(
                "RX-" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 20).toUpperCase());

        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setAppointment(appointment);
        prescription.setDiagnosis(diagnosis);
        prescription.setInstructions(instructions);
        prescription.setPrescriptionDate(LocalDateTime.now());
        prescription.setStatus(PrescriptionStatus.ACTIVE);

        Prescription savedPrescription =
                prescriptionRepository.save(prescription);

        for (PrescriptionItem item : items) {

            if (item.getDurationDays() == null || item.getDurationDays() <= 0) {
                throw new IllegalArgumentException(
                        "Duration must be greater than zero");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be greater than zero");
            }

            item.setPrescription(savedPrescription);
            itemRepository.save(item);
        }

        return savedPrescription;
    }

    @Transactional
    public PrescriptionResponse updatePrescriptionStatus(
            Long prescriptionId,
            PrescriptionStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Prescription status cannot be null");
        }

        Prescription prescription =
                prescriptionRepository.findByIdForUpdate(prescriptionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Prescription not found"));

        PrescriptionStatus currentStatus = prescription.getStatus();

        if (currentStatus == PrescriptionStatus.DISPENSED
                || currentStatus == PrescriptionStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Final prescription status cannot be changed");
        }

        boolean validTransition =
                (currentStatus == PrescriptionStatus.ACTIVE
                        && (newStatus == PrescriptionStatus.PARTIALLY_DISPENSED
                        || newStatus == PrescriptionStatus.DISPENSED
                        || newStatus == PrescriptionStatus.CANCELLED))
                ||
                (currentStatus == PrescriptionStatus.PARTIALLY_DISPENSED
                        && newStatus == PrescriptionStatus.DISPENSED);

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid prescription status transition");
        }

        prescription.setStatus(newStatus);

        Prescription savedPrescription =
                prescriptionRepository.save(prescription);

        // DTO mapping happens inside the active transaction.
        // This prevents LazyInitializationException.
        return responseMapper.toResponse(savedPrescription);
    }
}
