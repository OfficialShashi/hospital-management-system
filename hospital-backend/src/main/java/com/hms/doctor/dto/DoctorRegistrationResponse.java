
package com.hms.doctor.dto;

import java.math.BigDecimal;

public class DoctorRegistrationResponse {

    private Long doctorId;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String licenseNumber;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String departmentName;
    private String availabilityStatus;
    private String message;

    public DoctorRegistrationResponse() {
    }

    public DoctorRegistrationResponse(
            Long doctorId,
            Long userId,
            String fullName,
            String email,
            String phone,
            String licenseNumber,
            String specialization,
            String qualification,
            Integer experienceYears,
            BigDecimal consultationFee,
            String departmentName,
            String availabilityStatus,
            String message) {

        this.doctorId = doctorId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.departmentName = departmentName;
        this.availabilityStatus = availabilityStatus;
        this.message = message;
    }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
