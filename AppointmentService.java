
package com.hms.appointment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.hms.appointment.dto.AppointmentResponseDTO;
import com.hms.doctor.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.appointment.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.patient.Patient;
import com.hms.patient.repository.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Optional<Appointment> getAppointmentByNumber(String appointmentNumber) {
        return appointmentRepository.findByAppointmentNumber(appointmentNumber);
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {

        var authentication = SecurityContextHolder
                .getContext().getAuthentication();

        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (isPatient) {
            Patient loggedInPatient = patientRepository
                    .findByUser_Email(authentication.getName())
                    .orElseThrow(() ->
                            new RuntimeException("Patient profile not found"));

            if (!loggedInPatient.getPatientId().equals(patientId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "You are not allowed to access another patient's appointments");
            }
        }

        List<Appointment> appointments =
                appointmentRepository.findByPatient_PatientId(patientId);

        if (isDoctor) {
            Doctor loggedInDoctor = doctorRepository
                    .findByUser_Email(authentication.getName())
                    .orElseThrow(() ->
                            new RuntimeException("Doctor profile not found"));

            return appointments.stream()
                    .filter(appointment -> appointment.getDoctor()
                            .getDoctorId().equals(loggedInDoctor.getDoctorId()))
                    .toList();
        }

        return appointments;
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {

        var authentication = SecurityContextHolder
                .getContext().getAuthentication();

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (isDoctor) {
            var loggedInDoctor = doctorRepository
                    .findByUser_Email(authentication.getName())
                    .orElseThrow(() -> new RuntimeException(
                            "Doctor profile not found"));

            if (!loggedInDoctor.getDoctorId().equals(doctorId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "You are not allowed to access another doctor's appointments");
            }
        }

        return appointmentRepository.findByDoctor_DoctorId(doctorId);
    }

    public List<Appointment> getAppointmentsByDepartment(Long departmentId) {
        return appointmentRepository.findByDepartment_DepartmentId(departmentId);
    }

    public List<Appointment> getAppointmentsByStatus(
            Appointment.AppointmentStatus status) {
        return appointmentRepository.findByAppointmentStatus(status);
    }

    // Update appointment status
    @Transactional
    public Appointment updateAppointmentStatus(
            Long appointmentId,
            Appointment.AppointmentStatus newStatus) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Appointment not found"));

        // Verify doctor ownership
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (isDoctor && !appointment.getDoctor().getUser().getEmail()
                .equals(authentication.getName())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can update only your own appointments");
        }

        Appointment.AppointmentStatus currentStatus =
                appointment.getAppointmentStatus();

        if (currentStatus == Appointment.AppointmentStatus.COMPLETED
                || currentStatus == Appointment.AppointmentStatus.CANCELLED
                || currentStatus == Appointment.AppointmentStatus.NO_SHOW) {

            throw new IllegalArgumentException(
                    "Final appointment status cannot be changed");
        }

        if (newStatus == Appointment.AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException(
                    "Appointment cannot be changed back to BOOKED");
        }

        appointment.setAppointmentStatus(newStatus);

        return appointmentRepository.save(appointment);
    }
    


	@Transactional
	public Appointment rescheduleAppointment(
	        Long appointmentId,
	        LocalDateTime newDateTime) {
	
	    Appointment appointment = appointmentRepository.findById(appointmentId)
	            .orElseThrow(() ->
	                    new IllegalArgumentException("Appointment not found"));
	
	    // Patient ownership verification
	    var authentication = SecurityContextHolder
	            .getContext().getAuthentication();
	
	    boolean isPatient = authentication.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
	
	    if (isPatient && !appointment.getPatient().getUser().getEmail()
	            .equals(authentication.getName())) {
	
	        throw new org.springframework.security.access.AccessDeniedException(
	                "You can reschedule only your own appointments");
	    }
	
	    Appointment.AppointmentStatus currentStatus =
	            appointment.getAppointmentStatus();
	
	    if (currentStatus == Appointment.AppointmentStatus.COMPLETED
	            || currentStatus == Appointment.AppointmentStatus.NO_SHOW) {
	
	        throw new IllegalArgumentException(
	                "This appointment cannot be rescheduled");
	    }
	
	    boolean alreadyBooked = appointmentRepository
	            .existsByDoctor_DoctorIdAndAppointmentDateTimeAndAppointmentStatusInAndAppointmentIdNot(
	                    appointment.getDoctor().getDoctorId(),
	                    newDateTime,
	                    List.of(
	                            Appointment.AppointmentStatus.BOOKED,
	                            Appointment.AppointmentStatus.CONFIRMED
	                    ),
	                    appointmentId
	            );
	
	    if (alreadyBooked) {
	        throw new IllegalArgumentException(
	                "Doctor already has an appointment at this date and time");
	    }
	
	    appointment.setAppointmentDateTime(newDateTime);
	
	    // Cancelled appointment becomes BOOKED again
	    if (currentStatus == Appointment.AppointmentStatus.CANCELLED) {
	        appointment.setAppointmentStatus(
	                Appointment.AppointmentStatus.BOOKED);
	
	        appointment.setCancellationReason(null);
	    }
	
	    return appointmentRepository.save(appointment);
	}

    
    
    @Transactional
    public Appointment cancelAppointment(
            Long appointmentId,
            String cancellationReason) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Appointment not found"));

        // Patient ownership verification
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        if (isPatient && !appointment.getPatient().getUser().getEmail()
                .equals(authentication.getName())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can cancel only your own appointments");
        }

        Appointment.AppointmentStatus currentStatus =
                appointment.getAppointmentStatus();

        if (currentStatus == Appointment.AppointmentStatus.COMPLETED
                || currentStatus == Appointment.AppointmentStatus.CANCELLED
                || currentStatus == Appointment.AppointmentStatus.NO_SHOW) {

            throw new IllegalArgumentException(
                    "This appointment cannot be cancelled");
        }

        if (cancellationReason == null || cancellationReason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason is required");
        }

        appointment.setAppointmentStatus(
                Appointment.AppointmentStatus.CANCELLED);

        appointment.setCancellationReason(cancellationReason.trim());

        return appointmentRepository.save(appointment);
    }
    
    public List<AppointmentResponseDTO> getAppointmentDTOsByPatient(
            Long patientId) {

        return getAppointmentsByPatient(patientId)
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
    }
    
    public List<AppointmentResponseDTO> getAppointmentDTOsByDoctor(
            Long doctorId) {

        return getAppointmentsByDoctor(doctorId)
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
    }
    
    
    public List<AppointmentResponseDTO> getAllAppointmentDTOs() {
        return getAllAppointments()
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
    }
    
    public AppointmentResponseDTO getAppointmentDTOById(Long id) {

        Appointment appointment = getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        return new AppointmentResponseDTO(appointment);
    }
    
    public AppointmentResponseDTO getAppointmentDTOByNumber(
            String appointmentNumber) {

        Appointment appointment = appointmentRepository
                .findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() ->
                        new RuntimeException("Appointment not found"));

        return new AppointmentResponseDTO(appointment);
    }
    
    public List<AppointmentResponseDTO> getAppointmentDTOsByDepartment(
            Long departmentId) {

        return getAppointmentsByDepartment(departmentId)
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
    }
    
    public List<AppointmentResponseDTO> getAppointmentDTOsByStatus(
            Appointment.AppointmentStatus status) {

        return getAppointmentsByStatus(status)
                .stream()
                .map(AppointmentResponseDTO::new)
                .toList();
    }
	
}
