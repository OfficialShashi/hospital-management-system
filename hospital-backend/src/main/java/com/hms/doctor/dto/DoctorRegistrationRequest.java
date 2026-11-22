
package com.hms.doctor.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public class DoctorRegistrationRequest {

    @NotBlank(message = "Doctor name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$",
             message = "Phone must contain 10 to 15 digits")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    private String password;

    @NotNull(message = "Department is required")
    @Positive(message = "Invalid department ID")
    private Long departmentId;

    @NotBlank(message = "License number is required")
    @Size(max = 50)
    private String licenseNumber;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100)
    private String specialization;

    @Size(max = 150)
    private String qualification;

    @Min(0)
    @Max(60)
    private Integer experienceYears = 0;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal consultationFee;

    public DoctorRegistrationRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }
}
