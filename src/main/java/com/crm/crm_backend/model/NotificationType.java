package com.crm.crm_backend.model;

public enum NotificationType {
    INFO("Information"),
    SUCCESS("Success"),
    WARNING("Warning"),
    ERROR("Error"),
    LEAD_CREATED("Lead Created"),
    ORDER_APPROVAL("Order Approval"),
    INVOICE_PAID("Invoice Paid"),
    INVOICE_REJECTED("Invoice Rejected"),
    INVOICE_APPROVED("Invoice Approved"),
    SYSTEM_ALERT("System Alert"),
    TASK_ASSIGNED("Task Assigned"),
    DEAL_WON("Deal Won"),
    DEAL_LOST("Deal Lost"),
    PAYMENT_RECEIVED("Payment Received"),
    CUSTOMER_INQUIRY("Customer Inquiry");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
