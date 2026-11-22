
package com.hms.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.appointment.Appointment;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentNumber(
            String appointmentNumber);

    boolean existsByAppointmentNumber(String appointmentNumber);

    List<Appointment> findByPatient_PatientId(Long patientId);

    List<Appointment> findByDoctor_DoctorId(Long doctorId);

    // Get appointments of logged-in patient
    List<Appointment> findByPatient_User_Email(String email);

    // Get appointments of logged-in doctor
    List<Appointment> findByDoctor_User_Email(String email);

    List<Appointment> findByDepartment_DepartmentId(Long departmentId);

    List<Appointment> findByAppointmentStatus(
            Appointment.AppointmentStatus status);

    // Count appointments by status
    long countByAppointmentStatus(
            Appointment.AppointmentStatus status);

    // Check doctor availability for a specific appointment time
    boolean existsByDoctor_DoctorIdAndAppointmentDateTimeAndAppointmentStatusIn(
            Long doctorId,
            LocalDateTime appointmentDateTime,
            List<Appointment.AppointmentStatus> statuses);

    boolean existsByDoctor_DoctorIdAndAppointmentDateTimeAndAppointmentStatusInAndAppointmentIdNot(
            Long doctorId,
            LocalDateTime appointmentDateTime,
            List<Appointment.AppointmentStatus> statuses,
            Long appointmentId);

    List<Appointment> findByAppointmentDateTimeBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime);
}
