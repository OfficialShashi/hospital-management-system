
package com.hms.doctor.service;

import java.util.HashSet;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.department.Department;
import com.hms.department.repository.DepartmentRepository;
import com.hms.doctor.Doctor;
import com.hms.doctor.dto.DoctorRegistrationRequest;
import com.hms.doctor.dto.DoctorRegistrationResponse;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;
import com.hms.user.User;
import com.hms.user.repository.UserRepository;

@Service
public class DoctorRegistrationService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorRegistrationService(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            DepartmentRepository departmentRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DoctorRegistrationResponse registerDoctor(
            DoctorRegistrationRequest request) {

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

        // 3. Check duplicate license number
        if (doctorRepository.existsByLicenseNumber(
                request.getLicenseNumber())) {

            throw new IllegalArgumentException(
                    "Medical license number already exists");
        }

        // 4. Find department
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Department not found"));

        if (department.getStatus()
                != Department.DepartmentStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Selected department is inactive");
        }

        // 5. Find DOCTOR role
        Role doctorRole = roleRepository.findByRoleName("DOCTOR");

        if (doctorRole == null) {
            throw new IllegalStateException(
                    "DOCTOR role not found in database");
        }

        // 6. Create user account
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));

        user.setRoles(new HashSet<>());
        user.getRoles().add(doctorRole);

        User savedUser = userRepository.save(user);

        // 7. Create doctor profile
        Doctor doctor = new Doctor();

        doctor.setUser(savedUser);
        doctor.setDepartment(department);
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());

        doctor.setExperienceYears(
                request.getExperienceYears() == null
                        ? 0
                        : request.getExperienceYears());

        doctor.setConsultationFee(request.getConsultationFee());

        doctor.setAvailabilityStatus(
                Doctor.AvailabilityStatus.AVAILABLE);

        Doctor savedDoctor = doctorRepository.save(doctor);

        // 8. Return response
        return new DoctorRegistrationResponse(
                savedDoctor.getDoctorId(),
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedDoctor.getLicenseNumber(),
                savedDoctor.getSpecialization(),
                savedDoctor.getQualification(),
                savedDoctor.getExperienceYears(),
                savedDoctor.getConsultationFee(),
                department.getDepartmentName(),
                savedDoctor.getAvailabilityStatus().name(),
                "Doctor registered successfully"
        );
    }
}
