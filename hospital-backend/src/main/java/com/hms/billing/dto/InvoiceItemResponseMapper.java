
package com.hms.billing.dto;

import org.springframework.stereotype.Component;

import com.hms.billing.InvoiceItem;

@Component
public class InvoiceItemResponseMapper {

    public InvoiceItemResponse toResponse(InvoiceItem item) {

        InvoiceItemResponse response = new InvoiceItemResponse();

        response.setInvoiceItemId(item.getInvoiceItemId());
        response.setItemType(item.getItemType());
        response.setDescription(item.getDescription());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setLineTotal(item.getLineTotal());
        response.setSourceId(item.getSourceId());
        response.setCreatedAt(item.getCreatedAt());

        return response;
    }
}
