
package com.hms.appointment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.hms.appointment.Appointment;
import com.hms.appointment.dto.AppointmentBookingRequest;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.doctor.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.patient.Patient;
import com.hms.patient.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class AppointmentBookingService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Appointment bookAppointment(
            AppointmentBookingRequest request) {

    	Authentication authentication = SecurityContextHolder
    	        .getContext().getAuthentication();

    	boolean isPatient = authentication.getAuthorities().stream()
    	        .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

    	Patient patient;

    	if (isPatient) {

    	    patient = patientRepository
    	            .findByUser_Email(authentication.getName())
    	            .orElseThrow(() ->
    	                    new IllegalArgumentException("Patient profile not found"));

    	} else {

    	    patient = patientRepository
    	            .findById(request.getPatientId())
    	            .orElseThrow(() ->
    	                    new IllegalArgumentException("Patient not found"));
    	}

        if (patient.getUser() == null
                || patient.getUser().getAccountStatus()
                        != com.hms.user.User.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Patient account is inactive");
        }

        Doctor doctor = doctorRepository
                .findById(request.getDoctorId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        if (doctor.getUser().getAccountStatus()
                != com.hms.user.User.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Doctor account is inactive");
        }

        if (doctor.getAvailabilityStatus()
                != Doctor.AvailabilityStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "Doctor is currently unavailable");
        }

		boolean alreadyBooked = appointmentRepository
		        .existsByDoctor_DoctorIdAndAppointmentDateTimeAndAppointmentStatusIn(
		                doctor.getDoctorId(),
		                request.getAppointmentDateTime(),
		                List.of(
		                        Appointment.AppointmentStatus.BOOKED,
		                        Appointment.AppointmentStatus.CONFIRMED
		                )
		        );

		if (alreadyBooked) {
		    throw new IllegalArgumentException(
		            "Doctor already has an appointment at this date and time");
		}


        Appointment appointment = new Appointment();

        appointment.setAppointmentNumber(
                "APT-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase());

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDepartment(doctor.getDepartment());
        appointment.setAppointmentDateTime(
                request.getAppointmentDateTime());
        appointment.setReason(request.getReason());
        appointment.setAppointmentStatus(
                Appointment.AppointmentStatus.BOOKED);

        return appointmentRepository.save(appointment);
    }
}
