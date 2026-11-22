
package com.hms.user.dto;

import java.time.LocalDateTime;

import com.hms.user.User.AccountStatus;

public class UserResponseDTO {

    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;

    public UserResponseDTO(Long userId, String fullName,
            String email, String phone,
            AccountStatus accountStatus,
            LocalDateTime createdAt) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
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

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
