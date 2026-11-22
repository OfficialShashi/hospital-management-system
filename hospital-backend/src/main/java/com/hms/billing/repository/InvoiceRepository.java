
package com.hms.billing.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hms.billing.Invoice;
import com.hms.billing.Invoice.InvoiceStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user"
    })
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Override
    @EntityGraph(attributePaths = {
        "patient",
        "patient.user"
    })
    List<Invoice> findAll();

    @Override
    @EntityGraph(attributePaths = {
        "patient",
        "patient.user"
    })
    Optional<Invoice> findById(Long invoiceId);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user"
    })
    List<Invoice> findByPatient_PatientId(Long patientId);

    @EntityGraph(attributePaths = {
        "patient",
        "patient.user"
    })
    List<Invoice> findByInvoiceStatus(InvoiceStatus invoiceStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"patient", "patient.user"})
    @Query("SELECT i FROM Invoice i WHERE i.invoiceId = :invoiceId")
    Optional<Invoice> findByIdForUpdate(
            @Param("invoiceId") Long invoiceId);
}
