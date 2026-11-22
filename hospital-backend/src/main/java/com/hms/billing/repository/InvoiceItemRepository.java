
package com.hms.billing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.billing.InvoiceItem;

@Repository
public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, Long> {

    @EntityGraph(attributePaths = {
        "invoice",
        "invoice.patient",
        "invoice.patient.user"
    })
    List<InvoiceItem> findAllByInvoice_InvoiceId(Long invoiceId);

    List<InvoiceItem> findByItemType(
            InvoiceItem.ItemType itemType);
}
