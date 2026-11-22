
package com.hms.doctor.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.doctor.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Doctor> findByDepartment_DepartmentId(Long departmentId);

    List<Doctor> findByAvailabilityStatus(Doctor.AvailabilityStatus status);

    Optional<Doctor> findByUser_Email(String email);
}
