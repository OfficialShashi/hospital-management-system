
package com.hms.laboratory.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import com.hms.appointment.Appointment;
import com.hms.appointment.Appointment.AppointmentStatus;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.doctor.Doctor;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.laboratory.LabOrder;
import com.hms.laboratory.LabOrderItem;
import com.hms.laboratory.LabTest;
import com.hms.laboratory.dto.LabOrderItemRequest;
import com.hms.laboratory.dto.LabOrderRequest;
import com.hms.laboratory.dto.LabTestResultRequest;
import com.hms.laboratory.repository.LabOrderItemRepository;
import com.hms.laboratory.repository.LabOrderRepository;
import com.hms.laboratory.repository.LabTestRepository;
import com.hms.patient.Patient;
import com.hms.patient.repository.PatientRepository;

@Service
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final LabOrderItemRepository labOrderItemRepository;
    private final LabTestRepository labTestRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public LabOrderService(
            LabOrderRepository labOrderRepository,
            LabOrderItemRepository labOrderItemRepository,
            LabTestRepository labTestRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository) {

        this.labOrderRepository = labOrderRepository;
        this.labOrderItemRepository = labOrderItemRepository;
        this.labTestRepository = labTestRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // Create Lab Order
    @Transactional
    public LabOrder createLabOrder(LabOrderRequest request) {

        Patient patient = patientRepository
                .findById(request.getPatientId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        Doctor doctor = doctorRepository
                .findById(request.getDoctorId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        if (patient.getUser() == null ||
                patient.getUser().getAccountStatus() !=
                        com.hms.user.User.AccountStatus.ACTIVE) {

            throw new IllegalArgumentException("Patient is not active");
        }

        if (doctor.getUser() == null ||
                doctor.getUser().getAccountStatus() !=
                        com.hms.user.User.AccountStatus.ACTIVE) {

            throw new IllegalArgumentException("Doctor is not active");
        }

        if (doctor.getAvailabilityStatus() !=
                Doctor.AvailabilityStatus.AVAILABLE) {

            throw new IllegalArgumentException("Doctor is unavailable");
        }

        Appointment appointment = null;

        if (request.getAppointmentId() != null) {

            appointment = appointmentRepository
                    .findById(request.getAppointmentId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Appointment not found"));

            if (!appointment.getPatient().getPatientId()
                    .equals(patient.getPatientId())) {

                throw new IllegalArgumentException(
                        "Appointment does not belong to this patient");
            }

            if (!appointment.getDoctor().getDoctorId()
                    .equals(doctor.getDoctorId())) {

                throw new IllegalArgumentException(
                        "Appointment does not belong to this doctor");
            }

            if (appointment.getAppointmentStatus() !=
                    AppointmentStatus.COMPLETED) {

                throw new IllegalArgumentException(
                        "Appointment must be completed before lab ordering");
            }
        }


        LabOrder order = new LabOrder();

        order.setOrderNumber("LAB-" +
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase());

        order.setPatient(patient);
        order.setDoctor(doctor);
        order.setAppointment(appointment);
        order.setPriority(request.getPriority());
        order.setClinicalNotes(request.getClinicalNotes());
        order.setStatus(LabOrder.OrderStatus.ORDERED);

        List<LabOrderItem> items = new ArrayList<>();

        for (LabOrderItemRequest itemRequest : request.getItems()) {

            LabTest test = labTestRepository
                    .findById(itemRequest.getTestId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Lab test not found"));

            if (test.getStatus() != LabTest.TestStatus.ACTIVE) {

                throw new IllegalArgumentException(
                        "Lab test is inactive: " + test.getTestCode());
            }

            LabOrderItem item = new LabOrderItem();

            item.setLabOrder(order);
            item.setLabTest(test);
            item.setPrice(test.getPrice());
            item.setStatus(LabOrder.OrderStatus.ORDERED);

            items.add(item);
        }

        order.setItems(items);

        return labOrderRepository.save(order);
    }

 // Create Lab Order By Authenticated Doctor
    @Transactional
    public LabOrder createLabOrderForDoctor(
            LabOrderRequest request,
            Authentication authentication) {

        Doctor doctor = doctorRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor profile not found"));

        if (request.getAppointmentId() == null) {
            throw new IllegalArgumentException(
                    "Appointment ID is required for doctor lab orders");
        }

        Appointment appointment = appointmentRepository
                .findById(request.getAppointmentId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Appointment not found"));

        if (!appointment.getDoctor().getDoctorId()
                .equals(doctor.getDoctorId())) {

            throw new IllegalArgumentException(
                    "You are not authorized to create lab orders for this appointment");
        }

        if (!appointment.getPatient().getPatientId()
                .equals(request.getPatientId())) {

            throw new IllegalArgumentException(
                    "Appointment does not belong to this patient");
        }

        if (appointment.getAppointmentStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Appointment must be completed before lab ordering");
        }

        // Ignore the doctorId sent by the client.
        request.setDoctorId(doctor.getDoctorId());

        return createLabOrder(request);
    }

    // Collect Sample
    @Transactional
    public LabOrder collectSample(Long itemId) {

        LabOrderItem item = labOrderItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order item not found"));

        if (item.getStatus() != LabOrder.OrderStatus.ORDERED) {

            throw new IllegalArgumentException(
                    "Sample can only be collected for ORDERED items");
        }

        item.setStatus(LabOrder.OrderStatus.SAMPLE_COLLECTED);
        item.setSampleCollectedAt(LocalDateTime.now());

        LabOrder order = item.getLabOrder();

        updateOrderStatus(order);

        labOrderItemRepository.save(item);
        labOrderRepository.save(order);

        LabOrder updatedOrder = labOrderRepository
                .findById(order.getLabOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order not found"));

        initializeLabOrderItems(updatedOrder);

        return updatedOrder;
    }

    // Get All Lab Orders
    public List<LabOrder> getAllLabOrders() {
        return labOrderRepository.findAll();
    }

    // Get Lab Order By ID
    public LabOrder getLabOrderById(Long id) {

        return labOrderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order not found"));
    }

    // Get Lab Order By Number
    public LabOrder getLabOrderByNumber(String orderNumber) {

        return labOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order not found"));
    }

    // Get Orders By Patient
    public List<LabOrder> getOrdersByPatient(Long patientId) {
        return labOrderRepository.findByPatient_PatientId(patientId);
    }

    // Get Orders By Doctor
    public List<LabOrder> getOrdersByDoctor(Long doctorId) {
        return labOrderRepository.findByDoctor_DoctorId(doctorId);
    }


 // Get Lab Orders For Logged-in Doctor
    public List<LabOrder> getMyLabOrders(String email) {
        return labOrderRepository.findByDoctor_User_Email(email);
    }


    // Get Orders By Status
    public List<LabOrder> getOrdersByStatus(
            LabOrder.OrderStatus status) {

        return labOrderRepository.findByStatus(status);
    }

    // Start Processing
    @Transactional
    public LabOrder startProcessing(Long itemId) {

        LabOrderItem item = labOrderItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order item not found"));

        if (item.getStatus() != LabOrder.OrderStatus.SAMPLE_COLLECTED) {

            throw new IllegalArgumentException(
                    "Only collected samples can be processed");
        }

        item.setStatus(LabOrder.OrderStatus.PROCESSING);

        LabOrder order = item.getLabOrder();

        updateOrderStatus(order);

        labOrderItemRepository.save(item);
        labOrderRepository.save(order);

        LabOrder updatedOrder = labOrderRepository
                .findById(order.getLabOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order not found"));

        initializeLabOrderItems(updatedOrder);

        return updatedOrder;
    }

    // Complete Test
    @Transactional
    public LabOrder completeTest(
            Long itemId,
            LabTestResultRequest request) {

        LabOrderItem item = labOrderItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order item not found"));

        if (item.getStatus() != LabOrder.OrderStatus.PROCESSING) {

            throw new IllegalArgumentException(
                    "Only PROCESSING tests can be completed");
        }

        item.setResultValue(request.getResultValue());
        item.setResultUnit(request.getResultUnit());
        item.setReferenceRange(request.getReferenceRange());
        item.setResultNotes(request.getResultNotes());
        item.setResultDate(LocalDateTime.now());
        item.setStatus(LabOrder.OrderStatus.COMPLETED);

        LabOrder order = item.getLabOrder();

        labOrderItemRepository.save(item);

        updateOrderStatus(order);

        labOrderRepository.save(order);

        LabOrder updatedOrder = labOrderRepository
                .findById(order.getLabOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order not found"));

        initializeLabOrderItems(updatedOrder);

        return updatedOrder;
    }

    // Cancel Lab Order
    @Transactional
    public LabOrder cancelLabOrder(Long orderId) {

        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order not found"));

        if (order.getStatus() == LabOrder.OrderStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Completed lab orders cannot be cancelled");
        }

        if (order.getStatus() == LabOrder.OrderStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Lab order is already cancelled");
        }

        boolean hasCompletedItem = order.getItems().stream()
                .anyMatch(item ->
                        item.getStatus() == LabOrder.OrderStatus.COMPLETED);

        if (hasCompletedItem) {

            throw new IllegalArgumentException(
                    "Lab order with completed tests cannot be cancelled");
        }

        for (LabOrderItem item : order.getItems()) {
            item.setStatus(LabOrder.OrderStatus.CANCELLED);
        }

        labOrderItemRepository.saveAll(order.getItems());

        order.setStatus(LabOrder.OrderStatus.CANCELLED);

        labOrderRepository.save(order);

        LabOrder updatedOrder = labOrderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Lab order not found"));

        initializeLabOrderItems(updatedOrder);

        return updatedOrder;
    }

    // Automatically Synchronize Parent Order Status
    private void updateOrderStatus(LabOrder order) {

        List<LabOrderItem> items = order.getItems();

        boolean allCompleted = !items.isEmpty() &&
                items.stream()
                        .allMatch(item ->
                                item.getStatus() == LabOrder.OrderStatus.COMPLETED);

        if (allCompleted) {

            order.setStatus(LabOrder.OrderStatus.COMPLETED);
            return;
        }

        boolean hasProcessing = items.stream()
                .anyMatch(item ->
                        item.getStatus() == LabOrder.OrderStatus.PROCESSING);

        if (hasProcessing) {

            order.setStatus(LabOrder.OrderStatus.PROCESSING);
            return;
        }

        boolean hasSampleCollected = items.stream()
                .anyMatch(item ->
                        item.getStatus() == LabOrder.OrderStatus.SAMPLE_COLLECTED);

        if (hasSampleCollected) {

            order.setStatus(LabOrder.OrderStatus.SAMPLE_COLLECTED);
            return;
        }

        order.setStatus(LabOrder.OrderStatus.ORDERED);
    }

    // Initialize Lab Order Items and LabTest References
    private void initializeLabOrderItems(LabOrder order) {

        Hibernate.initialize(order.getItems());

        for (LabOrderItem item : order.getItems()) {

            Hibernate.initialize(item.getLabTest());
        }
    }
}
