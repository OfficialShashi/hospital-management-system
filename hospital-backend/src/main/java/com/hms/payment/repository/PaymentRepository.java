
package com.hms.payment.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hms.payment.Payment;
import com.hms.payment.Payment.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByPaymentNumber(String paymentNumber);

    @EntityGraph(attributePaths = {
            "invoice",
            "invoice.patient",
            "invoice.patient.user"
    })
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    @Override
    @EntityGraph(attributePaths = {
            "invoice",
            "invoice.patient",
            "invoice.patient.user"
    })
    List<Payment> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "invoice",
            "invoice.patient",
            "invoice.patient.user"
    })
    Optional<Payment> findById(Long paymentId);

    @EntityGraph(attributePaths = {
            "invoice",
            "invoice.patient",
            "invoice.patient.user"
    })
    List<Payment> findByInvoice_InvoiceId(Long invoiceId);

    @EntityGraph(attributePaths = {
            "invoice",
            "invoice.patient",
            "invoice.patient.user"
    })
    List<Payment> findByInvoice_InvoiceIdOrderByPaymentDateDesc(
            Long invoiceId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "invoice",
            "invoice.patient",
            "invoice.patient.user"
    })
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findByIdForUpdate(
            @Param("paymentId") Long paymentId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.paymentStatus = :status
            """)
    BigDecimal getTotalAmountByPaymentStatus(
            @Param("status") PaymentStatus status);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.invoice.invoiceId = :invoiceId
            AND p.paymentStatus = :status
            """)
    BigDecimal getTotalAmountByInvoiceAndStatus(
            @Param("invoiceId") Long invoiceId,
            @Param("status") PaymentStatus status);
}
