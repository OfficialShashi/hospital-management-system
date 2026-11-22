
package com.hms.doctor.dto;

import java.math.BigDecimal;

public class DoctorResponseDTO {

    private Long doctorId;
    private Long userId;
    private String fullName;
    private String email;
    private String licenseNumber;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String availabilityStatus;
    private String departmentName;

    public DoctorResponseDTO(
            Long doctorId,
            Long userId,
            String fullName,
            String email,
            String licenseNumber,
            String specialization,
            String qualification,
            Integer experienceYears,
            BigDecimal consultationFee,
            String availabilityStatus,
            String departmentName) {

        this.doctorId = doctorId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.availabilityStatus = availabilityStatus;
        this.departmentName = departmentName;
    }

    public Long getDoctorId() { return doctorId; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getSpecialization() { return specialization; }
    public String getQualification() { return qualification; }
    public Integer getExperienceYears() { return experienceYears; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public String getDepartmentName() { return departmentName; }
}
