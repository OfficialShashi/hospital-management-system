
package com.hms.pharmacy.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.hms.pharmacy.Medicine;
import com.hms.pharmacy.MedicineBatch;
import com.hms.pharmacy.MedicineDispensing;
import com.hms.pharmacy.dto.MedicineDispensingRequest;
import com.hms.pharmacy.dto.MedicineDispensingResponse;
import com.hms.pharmacy.dto.MedicineDispensingResponseMapper;
import com.hms.pharmacy.repository.MedicineBatchRepository;
import com.hms.pharmacy.repository.MedicineDispensingRepository;
import com.hms.prescription.Prescription;
import com.hms.prescription.PrescriptionItem;
import com.hms.prescription.PrescriptionStatus;
import com.hms.prescription.repository.PrescriptionItemRepository;
import com.hms.prescription.repository.PrescriptionRepository;
import com.hms.user.User;
import com.hms.user.repository.UserRepository;

@Service
public class MedicineDispensingService {

    private final MedicineDispensingRepository dispensingRepository;
    private final MedicineBatchRepository batchRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final MedicineDispensingResponseMapper responseMapper;

    public MedicineDispensingService(
            MedicineDispensingRepository dispensingRepository,
            MedicineBatchRepository batchRepository,
            PrescriptionItemRepository prescriptionItemRepository,
            PrescriptionRepository prescriptionRepository,
            UserRepository userRepository,
            MedicineDispensingResponseMapper responseMapper) {

        this.dispensingRepository = dispensingRepository;
        this.batchRepository = batchRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.responseMapper = responseMapper;
    }

    @Transactional
    public MedicineDispensingResponse dispenseMedicine(
            MedicineDispensingRequest request) {

        // 1. Lock and find prescription item
        PrescriptionItem item = prescriptionItemRepository
                .findByIdForUpdate(request.getPrescriptionItemId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Prescription item not found"));

        Prescription prescription = item.getPrescription();

        // 2. Validate prescription status
        if (prescription.getStatus() != PrescriptionStatus.ACTIVE
                && prescription.getStatus()
                    != PrescriptionStatus.PARTIALLY_DISPENSED) {

            throw new IllegalArgumentException(
                    "Prescription is not available for dispensing");
        }

        // 3. Validate pharmacist account and role
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String authenticatedEmail = authentication.getName();

        User pharmacist = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Authenticated pharmacist not found"));

        if (pharmacist.getAccountStatus()
                != User.AccountStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Pharmacist account is not active");
        }

        boolean hasPharmacistRole = pharmacist.getRoles()
                .stream()
                .anyMatch(role ->
                    "PHARMACIST".equalsIgnoreCase(role.getRoleName()));

        if (!hasPharmacistRole) {
            throw new IllegalArgumentException(
                    "User does not have PHARMACIST role");
        }

        // 4. Calculate remaining prescription quantity
        Long alreadyDispensed = dispensingRepository
                .getTotalDispensedQuantity(item.getItemId());

        int remainingPrescriptionQuantity =
                item.getQuantity() - alreadyDispensed.intValue();

        if (remainingPrescriptionQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Prescription item is already fully dispensed");
        }

        if (request.getQuantity() > remainingPrescriptionQuantity) {
            throw new IllegalArgumentException(
                    "Dispensing quantity exceeds remaining prescription quantity: "
                    + remainingPrescriptionQuantity);
        }

        // 5. Lock medicine batch
        MedicineBatch batch = batchRepository
                .findByIdForUpdate(request.getBatchId())
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Medicine batch not found"));

        Medicine medicine = batch.getMedicine();

        // 6. Validate medicine
        if (medicine.getStatus() != Medicine.MedicineStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Medicine is inactive");
        }

        boolean medicineMatches =
                item.getMedicineName().equalsIgnoreCase(
                        medicine.getMedicineName())
                || (medicine.getGenericName() != null
                    && item.getMedicineName().equalsIgnoreCase(
                        medicine.getGenericName()));

        if (!medicineMatches) {
            throw new IllegalArgumentException(
                    "Selected batch medicine does not match prescription medicine");
        }

        // 7. Validate batch expiry and stock
        if (batch.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Expired medicine cannot be dispensed");
        }

        if (batch.getQuantityAvailable() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available quantity: "
                    + batch.getQuantityAvailable());
        }

        // 8. Reduce available stock
        batch.setQuantityAvailable(
                batch.getQuantityAvailable() - request.getQuantity());

        batchRepository.save(batch);

        // 9. Create dispensing transaction
        MedicineDispensing dispensing = new MedicineDispensing();

        dispensing.setPrescriptionItem(item);
        dispensing.setBatch(batch);
        dispensing.setQuantityDispensed(request.getQuantity());
        dispensing.setDispensedBy(pharmacist);

        MedicineDispensing savedDispensing =
                dispensingRepository.saveAndFlush(dispensing);

        int totalDispensed =
                alreadyDispensed.intValue() + request.getQuantity();

        // Check whether every item in prescription is fully dispensed
        var prescriptionItems = prescriptionItemRepository
                .findByPrescription_PrescriptionId(
                        prescription.getPrescriptionId());

        boolean allItemsDispensed = prescriptionItems.stream()
                .allMatch(prescriptionItem -> {

                    if (prescriptionItem.getItemId()
                            .equals(item.getItemId())) {
                        return totalDispensed
                                >= prescriptionItem.getQuantity();
                    }

                    Long total = dispensingRepository
                            .getTotalDispensedQuantity(
                                prescriptionItem.getItemId());

                    return total >= prescriptionItem.getQuantity();
                });

        if (allItemsDispensed) {
            prescription.setStatus(PrescriptionStatus.DISPENSED);
        } else {
            prescription.setStatus(
                    PrescriptionStatus.PARTIALLY_DISPENSED);
        }

        prescriptionRepository.save(prescription);

        // 11. Return response
        return responseMapper.toResponse(savedDispensing);
    }
    @Transactional(readOnly = true)
    public List<MedicineDispensingResponse> getDispensingByPrescriptionItem(
            Long itemId) {

        return dispensingRepository
                .findByPrescriptionItem_ItemId(itemId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }
}
