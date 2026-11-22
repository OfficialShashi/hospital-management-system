
package com.hms.user.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import com.hms.user.dto.AccountantCreateRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hms.user.dto.UserResponseDTO;
import com.hms.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users - ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersDTO());
    }

    // Get user by ID - ADMIN only
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id) {

        return userService.getUserByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get user by email - ADMIN only
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @PathVariable String email) {

        return userService.getUserByEmailDTO(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

 // Create Accountant - ADMIN only
    @PostMapping("/accountant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createAccountant(
            @Valid @RequestBody AccountantCreateRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createAccountant(request));
    }
}
