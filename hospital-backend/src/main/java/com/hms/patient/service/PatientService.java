
package com.hms.patient.service;

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;
import com.hms.appointment.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.patient.Patient;
import com.hms.patient.dto.PatientResponseDTO;
import com.hms.patient.dto.PatientUpdateRequestDTO;
import com.hms.patient.repository.PatientRepository;
import com.hms.user.User;
import com.hms.user.repository.UserRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(
            PatientRepository patientRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository) {

        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // Convert Patient entity into safe DTO
    private PatientResponseDTO convertToDTO(Patient patient) {

        User user = patient.getUser();

        return new PatientResponseDTO(
                patient.getPatientId(),
                patient.getPatientNumber(),
                user != null ? user.getFullName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null,
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getAddress(),
                patient.getEmergencyContact(),
                patient.getCreatedAt(),
                user != null ? user.getAccountStatus() : null
        );
    }

    // Existing methods - preserved
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long patientId) {
        return patientRepository.findById(patientId);
    }

    public Optional<Patient> getPatientByNumber(String patientNumber) {
        return patientRepository.findByPatientNumber(patientNumber);
    }

    public Optional<Patient> getPatientByUserId(Long userId) {
        return patientRepository.findByUser_UserId(userId);
    }

    public boolean patientNumberExists(String patientNumber) {
        return patientRepository.existsByPatientNumber(patientNumber);
    }

    public boolean patientExistsByUserId(Long userId) {
        return patientRepository.existsByUser_UserId(userId);
    }

    // Safe DTO methods
    public List<PatientResponseDTO> getAllPatientsDTO() {
        return patientRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<PatientResponseDTO> getPatientByIdDTO(Long patientId) {
        return patientRepository.findById(patientId)
                .map(this::convertToDTO);
    }

    public Optional<PatientResponseDTO> getPatientByNumberDTO(
            String patientNumber) {

        return patientRepository.findByPatientNumber(patientNumber)
                .map(this::convertToDTO);
    }

    public Optional<PatientResponseDTO> getPatientByUserIdDTO(Long userId) {
        return patientRepository.findByUser_UserId(userId)
                .map(this::convertToDTO);
    }

    public Optional<PatientResponseDTO> getPatientByEmailDTO(String email) {
        return patientRepository.findByUser_Email(email)
                .map(this::convertToDTO);
    }

    // Update patient
    public Patient updatePatient(
            Long patientId,
            PatientUpdateRequestDTO updatedPatient) {

        Patient existingPatient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        if (updatedPatient.getDateOfBirth() != null) {
            existingPatient.setDateOfBirth(updatedPatient.getDateOfBirth());
        }

        if (updatedPatient.getGender() != null) {
            existingPatient.setGender(updatedPatient.getGender());
        }

        if (updatedPatient.getBloodGroup() != null) {
            existingPatient.setBloodGroup(updatedPatient.getBloodGroup());
        }

        if (updatedPatient.getAddress() != null) {
            existingPatient.setAddress(updatedPatient.getAddress());
        }

        if (updatedPatient.getEmergencyContact() != null) {
            existingPatient.setEmergencyContact(updatedPatient.getEmergencyContact());
        }

        return patientRepository.save(existingPatient);
    }

    // Get patient by email
    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByUser_Email(email);
    }

    // Deactivate patient
    @Transactional
    public Patient deactivatePatient(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        User user = userRepository.findById(
                patient.getUser().getUserId()
        ).orElseThrow(() ->
                new IllegalArgumentException("User account not found"));

        user.setAccountStatus(User.AccountStatus.INACTIVE);

        userRepository.save(user);

        return patient;
    }

    // Reactivate patient
    @Transactional
    public Patient reactivatePatient(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        User user = userRepository.findById(
                patient.getUser().getUserId()
        ).orElseThrow(() ->
                new IllegalArgumentException("User account not found"));

        user.setAccountStatus(User.AccountStatus.ACTIVE);

        userRepository.save(user);

        return patient;
    }

    public Optional<PatientResponseDTO> getPatientByUserIdDTO(
            Long userId,
            Authentication authentication) {

        boolean isPatient = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_PATIENT"));

        if (isPatient) {
            Patient patient = patientRepository.findByUser_Email(
                    authentication.getName()
            ).orElseThrow(() ->
                    new IllegalArgumentException("Patient profile not found"));

            if (!patient.getUser().getUserId().equals(userId)) {
                throw new AccessDeniedException(
                        "You are not allowed to access another patient's profile");
            }

            return Optional.of(convertToDTO(patient));
        }

        return patientRepository.findByUser_UserId(userId)
                .map(this::convertToDTO);
    }

 // Get only patients linked to logged-in doctor's appointments
    public List<PatientResponseDTO> getPatientsByDoctor(
            Authentication authentication) {

        List<Appointment> appointments =
                appointmentRepository.findByDoctor_User_Email(
                        authentication.getName()
                );

        Set<Long> patientIds = new HashSet<>();

        return appointments.stream()
                .map(Appointment::getPatient)
                .filter(patient -> patient != null)
                .filter(patient -> patientIds.add(patient.getPatientId()))
                .map(this::convertToDTO)
                .toList();
    }


    // Get individual patient only if linked to logged-in doctor
    public Optional<PatientResponseDTO> getDoctorPatientByIdDTO(
            Long patientId,
            Authentication authentication) {

        boolean hasAccess = appointmentRepository
                .findByDoctor_User_Email(authentication.getName())
                .stream()
                .anyMatch(appointment ->
                        appointment.getPatient() != null
                        && appointment.getPatient()
                                .getPatientId().equals(patientId)
                );

        if (!hasAccess) {
            throw new AccessDeniedException(
                    "You are not allowed to access this patient");
        }

        return patientRepository.findById(patientId)
                .map(this::convertToDTO);
    }
}
