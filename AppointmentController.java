
package com.hms.appointment.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.hms.appointment.dto.AppointmentBookingRequest;
import com.hms.appointment.dto.AppointmentRescheduleRequest;
import jakarta.validation.Valid;
import com.hms.appointment.service.AppointmentBookingService;
import com.hms.appointment.Appointment;
import com.hms.appointment.service.AppointmentService;
import com.hms.appointment.dto.AppointmentCancellationRequest;
import com.hms.appointment.dto.AppointmentResponseDTO;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;

    public AppointmentController(
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService) {

        this.appointmentService = appointmentService;
        this.appointmentBookingService = appointmentBookingService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {

        return ResponseEntity.ok(
                appointmentService.getAllAppointmentDTOs());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentDTOById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/number/{appointmentNumber}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentByNumber(
            @PathVariable String appointmentNumber) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentDTOByNumber(appointmentNumber));
    }
    
    

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT', 'DOCTOR')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByPatient(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentDTOsByPatient(patientId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByDoctor(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentDTOsByDoctor(doctorId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentDTOsByDepartment(departmentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByStatus(
            @PathVariable Appointment.AppointmentStatus status) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentDTOsByStatus(status));
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @PostMapping("/book")
    public ResponseEntity<AppointmentResponseDTO> bookAppointment(
            @Valid @RequestBody AppointmentBookingRequest request) {

        Appointment appointment =
                appointmentBookingService.bookAppointment(request);

        AppointmentResponseDTO response =
                new AppointmentResponseDTO(appointment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @PathVariable Appointment.AppointmentStatus status) {

        Appointment appointment =
                appointmentService.updateAppointmentStatus(id, status);

        return ResponseEntity.ok(
                new AppointmentResponseDTO(appointment));
    }
    
    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRescheduleRequest request) {

        Appointment appointment = appointmentService.rescheduleAppointment(
                id,
                request.getAppointmentDateTime()
        );

        return ResponseEntity.ok(
                new AppointmentResponseDTO(appointment));
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentCancellationRequest request) {

        Appointment appointment = appointmentService.cancelAppointment(
                id,
                request.getCancellationReason()
        );

        return ResponseEntity.ok(
                new AppointmentResponseDTO(appointment));
    }

}
