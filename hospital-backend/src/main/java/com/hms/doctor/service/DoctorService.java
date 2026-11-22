
package com.hms.doctor.service;

import java.util.List;
import java.util.Optional;
import com.hms.department.Department;
import com.hms.department.repository.DepartmentRepository;
import com.hms.doctor.dto.DoctorUpdateRequest;
import com.hms.user.User;
import java.util.Set;
import com.hms.doctor.dto.DoctorCreateRequestDTO;
import com.hms.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import com.hms.doctor.dto.DoctorResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import com.hms.doctor.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(
            DoctorRepository doctorRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> getDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId);
    }

    public Optional<Doctor> getDoctorByLicenseNumber(String licenseNumber) {
        return doctorRepository.findByLicenseNumber(licenseNumber);
    }

    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartment_DepartmentId(departmentId);
    }

    public List<Doctor> getDoctorsByAvailability(Doctor.AvailabilityStatus status) {
        return doctorRepository.findByAvailabilityStatus(status);
    }
    public Doctor updateAvailability(
            Long doctorId,
            Doctor.AvailabilityStatus status) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        doctor.setAvailabilityStatus(status);

        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(
            Long doctorId,
            DoctorUpdateRequest request) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Department not found"));

        if (department.getStatus() != Department.DepartmentStatus.ACTIVE) {
            throw new IllegalArgumentException("Department is inactive");
        }

        doctor.setDepartment(department);
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());

        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor deactivateDoctor(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        User user = userRepository.findById(
                doctor.getUser().getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User account not found"));

        user.setAccountStatus(User.AccountStatus.INACTIVE);
        userRepository.save(user);

        return doctor;
    }

    @Transactional
    public Doctor reactivateDoctor(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        User user = userRepository.findById(
                doctor.getUser().getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User account not found"));

        user.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(user);

        return doctor;
    }

    private DoctorResponseDTO convertToDTO(Doctor doctor) {

        return new DoctorResponseDTO(
                doctor.getDoctorId(),
                doctor.getUser().getUserId(),
                doctor.getUser().getFullName(),
                doctor.getUser().getEmail(),
                doctor.getLicenseNumber(),
                doctor.getSpecialization(),
                doctor.getQualification(),
                doctor.getExperienceYears(),
                doctor.getConsultationFee(),
                doctor.getAvailabilityStatus().name(),
                doctor.getDepartment().getDepartmentName()
        );
    }

    public List<DoctorResponseDTO> getAllDoctorsDTO() {

        return doctorRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<DoctorResponseDTO> getDoctorByIdDTO(Long doctorId) {

        return doctorRepository.findById(doctorId)
                .map(this::convertToDTO);
    }
    public DoctorResponseDTO getMyDoctorProfile(Authentication authentication) {

        Doctor doctor = doctorRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor profile not found"));

        return convertToDTO(doctor);
    }
    public Optional<DoctorResponseDTO> getDoctorByLicenseNumberDTO(
            String licenseNumber) {

        return doctorRepository.findByLicenseNumber(licenseNumber)
                .map(this::convertToDTO);
    }
    public List<DoctorResponseDTO> getDoctorsByDepartmentDTO(Long departmentId) {

        return doctorRepository.findByDepartment_DepartmentId(departmentId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<DoctorResponseDTO> getDoctorsByAvailabilityDTO(
            Doctor.AvailabilityStatus status) {

        return doctorRepository.findByAvailabilityStatus(status)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public DoctorResponseDTO updateAvailabilityDTO(
            Long doctorId,
            Doctor.AvailabilityStatus status) {

        Doctor doctor = updateAvailability(doctorId, status);

        return convertToDTO(doctor);
    }

    public DoctorResponseDTO updateDoctorDTO(
            Long doctorId,
            DoctorUpdateRequest request) {

        Doctor doctor = updateDoctor(doctorId, request);

        return convertToDTO(doctor);
    }

    public DoctorResponseDTO deactivateDoctorDTO(Long doctorId) {

        Doctor doctor = deactivateDoctor(doctorId);

        return convertToDTO(doctor);
    }

    public DoctorResponseDTO reactivateDoctorDTO(Long doctorId) {

        Doctor doctor = reactivateDoctor(doctorId);

        return convertToDTO(doctor);
    }

    public DoctorResponseDTO updateAvailabilityDTO(
            Long doctorId,
            Doctor.AvailabilityStatus status,
            Authentication authentication) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !doctor.getUser().getEmail()
                .equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException(
                    "You are not allowed to update another doctor's availability");
        }

        doctor.setAvailabilityStatus(status);

        return convertToDTO(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorResponseDTO createDoctor(DoctorCreateRequestDTO request) {

        String email = request.getEmail().trim().toLowerCase();

        String phone = request.getPhone() == null
                ? null : request.getPhone().trim();

        String licenseNumber = request.getLicenseNumber().trim();

        // Check duplicate email
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check duplicate phone
        if (phone != null && !phone.isBlank()
                && userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone already exists");
        }

        // Check duplicate medical license
        if (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("License number already exists");
        }

        // Find DOCTOR role
        Role doctorRole = roleRepository.findByRoleName("DOCTOR");

        if (doctorRole == null) {
            throw new IllegalStateException("DOCTOR role not found");
        }

        // Find department
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Department not found"));

        if (department.getStatus() != Department.DepartmentStatus.ACTIVE) {
            throw new IllegalArgumentException("Department is inactive");
        }

        // Create User account
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(phone == null || phone.isBlank() ? null : phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setRoles(Set.of(doctorRole));

        User savedUser = userRepository.save(user);

        // Create Doctor profile
        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setDepartment(department);
        doctor.setLicenseNumber(licenseNumber);
        doctor.setSpecialization(request.getSpecialization().trim());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAvailabilityStatus(Doctor.AvailabilityStatus.AVAILABLE);

        Doctor savedDoctor = doctorRepository.save(doctor);

        return convertToDTO(savedDoctor);
    }

}
