
package com.hms.laboratory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.laboratory.LabOrderItem;

@Repository
public interface LabOrderItemRepository
        extends JpaRepository<LabOrderItem, Long> {

    @EntityGraph(attributePaths = {
        "labOrder",
        "labOrder.patient",
        "labOrder.patient.user",
        "labOrder.doctor",
        "labOrder.doctor.user",
        "labOrder.doctor.department",
        "labTest"
    })
    List<LabOrderItem> findByLabOrder_LabOrderId(Long labOrderId);

    @Override
    @EntityGraph(attributePaths = {
        "labOrder",
        "labOrder.patient",
        "labOrder.patient.user",
        "labOrder.doctor",
        "labOrder.doctor.user",
        "labOrder.doctor.department",
        "labTest"
    })
    Optional<LabOrderItem> findById(Long id);

    @EntityGraph(attributePaths = {
        "labOrder",
        "labOrder.patient",
        "labOrder.patient.user",
        "labOrder.doctor",
        "labOrder.doctor.user",
        "labOrder.doctor.department",
        "labTest"
    })
    List<LabOrderItem> findByStatus(
        com.hms.laboratory.LabOrder.OrderStatus status);
}
