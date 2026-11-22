
package com.hms.patient.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.hms.user.User.AccountStatus;
import com.hms.patient.Patient.Gender;

public class PatientResponseDTO {

    private Long patientId;
    private String patientNumber;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String bloodGroup;
    private String address;
    private String emergencyContact;
    private LocalDateTime createdAt;
    private AccountStatus accountStatus;

    public PatientResponseDTO(
            Long patientId,
            String patientNumber,
            String fullName,
            String email,
            String phone,
            LocalDate dateOfBirth,
            Gender gender,
            String bloodGroup,
            String address,
            String emergencyContact,
            LocalDateTime createdAt,
            AccountStatus accountStatus) {

        this.patientId = patientId;
        this.patientNumber = patientNumber;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.createdAt = createdAt;
        this.accountStatus = accountStatus;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }
}
