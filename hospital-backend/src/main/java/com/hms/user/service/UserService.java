
package com.hms.user.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;
import com.hms.user.dto.AccountantCreateRequestDTO;
import com.hms.user.User;
import com.hms.user.dto.UserResponseDTO;
import com.hms.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Safe DTO: Get all users
    public List<UserResponseDTO> getAllUsersDTO() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getAccountStatus(),
                        user.getCreatedAt()))
                .toList();
    }

    // Safe DTO: Get user by ID
    public Optional<UserResponseDTO> getUserByIdDTO(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new UserResponseDTO(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getAccountStatus(),
                        user.getCreatedAt()));
    }

    // Safe DTO: Get user by email
    public Optional<UserResponseDTO> getUserByEmailDTO(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserResponseDTO(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getAccountStatus(),
                        user.getCreatedAt()));
    }

    // Check email exists
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Check phone exists
    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Transactional
    public UserResponseDTO createAccountant(
            AccountantCreateRequestDTO request) {

        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null
                ? null : request.getPhone().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (phone != null && !phone.isBlank()
                && userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone already exists");
        }

        Role accountantRole = roleRepository.findByRoleName("ACCOUNTANT");

        if (accountantRole == null) {
            throw new IllegalStateException("ACCOUNTANT role not found");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(phone == null || phone.isBlank() ? null : phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setRoles(Set.of(accountantRole));

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getAccountStatus(),
                savedUser.getCreatedAt());
    }
}
