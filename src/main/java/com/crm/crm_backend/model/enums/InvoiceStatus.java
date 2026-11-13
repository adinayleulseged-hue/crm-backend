package com.crm.crm_backend.model.enums;

public enum InvoiceStatus {
    DRAFT,           // Invoice created but not sent
    PENDING,         // Awaiting payment
    SENT,            // Sent to customer
    APPROVED,        // Finance approved, awaiting customer confirmation
    CONFIRMED,       // Customer confirmed payment
    PAID,            // Payment completed
    OVERDUE,         // Past due date
    CANCELLED        // Invoice cancelled
}
