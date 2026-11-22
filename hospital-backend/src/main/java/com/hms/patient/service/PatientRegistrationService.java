
package com.hms.patient.service;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.patient.Patient;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientRegistrationResponse;
import com.hms.patient.repository.PatientRepository;
import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;
import com.hms.user.User;
import com.hms.user.repository.UserRepository;

@Service
public class PatientRegistrationService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientRegistrationService(
            UserRepository userRepository,
            PatientRepository patientRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PatientRegistrationResponse registerPatient(
            PatientRegistrationRequest request) {

        // 1. Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already registered");
        }

        // 2. Check duplicate phone
        if (request.getPhone() != null
                && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {

            throw new IllegalArgumentException(
                    "Phone number is already registered");
        }

        // 3. Find PATIENT role
        Role patientRole = roleRepository.findByRoleName("PATIENT");

        if (patientRole == null) {
            throw new IllegalStateException(
                    "PATIENT role not found in database");
        }

        // 4. Create user account
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // Always store encrypted password
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));

        user.setRoles(new HashSet<>());
        user.getRoles().add(patientRole);

        User savedUser = userRepository.save(user);

        // 5. Generate unique patient number
        String patientNumber = "PAT-" +
                UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase();

        // 6. Create patient profile
        Patient patient = new Patient();

        patient.setUser(savedUser);
        patient.setPatientNumber(patientNumber);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContact(request.getEmergencyContact());

        Patient savedPatient = patientRepository.save(patient);

        // 7. Return registration response
        return new PatientRegistrationResponse(
                savedPatient.getPatientId(),
                savedPatient.getPatientNumber(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedPatient.getDateOfBirth(),
                savedPatient.getGender() == null
                        ? null
                        : savedPatient.getGender().name(),
                "Patient registered successfully"
        );
    }
}
