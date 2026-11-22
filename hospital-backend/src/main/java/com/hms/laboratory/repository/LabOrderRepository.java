
package com.hms.laboratory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.laboratory.LabOrder;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    boolean existsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {
        "patient", "patient.user",
        "doctor", "doctor.user", "doctor.department",
        "appointment",
        "items", "items.labTest"
    })
    Optional<LabOrder> findByOrderNumber(String orderNumber);

    @Override
    @EntityGraph(attributePaths = {
        "patient", "patient.user",
        "doctor", "doctor.user", "doctor.department",
        "appointment",
        "items", "items.labTest"
    })
    List<LabOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {
        "patient", "patient.user",
        "doctor", "doctor.user", "doctor.department",
        "appointment",
        "items", "items.labTest"
    })
    Optional<LabOrder> findById(Long id);

    @EntityGraph(attributePaths = {
        "patient", "patient.user",
        "doctor", "doctor.user", "doctor.department",
        "appointment",
        "items", "items.labTest"
    })
    List<LabOrder> findByPatient_PatientId(Long patientId);

    @EntityGraph(attributePaths = {
        "patient", "patient.user",
        "doctor", "doctor.user", "doctor.department",
        "appointment",
        "items", "items.labTest"
    })
    List<LabOrder> findByDoctor_DoctorId(Long doctorId);

    @EntityGraph(attributePaths = {
    	    "patient", "patient.user",
    	    "doctor", "doctor.user", "doctor.department",
    	    "appointment",
    	    "items", "items.labTest"
    	})
    	List<LabOrder> findByStatus(LabOrder.OrderStatus status);

    @EntityGraph(attributePaths = {
    	    "patient", "patient.user",
    	    "doctor", "doctor.user", "doctor.department",
    	    "appointment",
    	    "items", "items.labTest"
    	})
    	List<LabOrder> findByDoctor_User_Email(String email);
}
