package com.hms.doctor.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public class DoctorUpdateRequest {

    @NotNull
    @Positive
    private Long departmentId;

    @NotBlank
    @Size(max = 100)
    private String specialization;

    @Size(max = 150)
    private String qualification;

    @NotNull
    @Min(0)
    @Max(60)
    private Integer experienceYears;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal consultationFee;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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