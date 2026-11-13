package com.crm.crm_backend.model.enums;

/**
 * Enum representing the workflow status of a deal/opportunity in the approval process
 * Flow: DRAFT → PENDING_APPROVAL → APPROVED → ORDER_PLACED → INVOICE_GENERATED 
 *       → PAYMENT_SUBMITTED → PAYMENT_APPROVED → QUALIFIED/CLOSED_WON/CLOSED_LOST
 */
public enum WorkflowStatus {
    DRAFT,                    // Initial state, customer hasn't submitted yet
    PENDING_APPROVAL,         // Customer submitted, waiting for Admin/Sales Manager approval
    APPROVED,                 // Admin approved, customer can now place order
    REJECTED,                 // Admin rejected the deal
    NEEDS_REVISION,           // Customer needs to revise and resubmit
    ORDER_PLACED,             // Customer placed the order
    INVOICE_GENERATED,        // System automatically created invoice
    PAYMENT_SUBMITTED,        // Customer submitted payment for the invoice
    PAYMENT_APPROVED,         // Finance approved the payment (invoice is now PAID)
    QUALIFIED,                // Admin/Sales Manager marked deal as Qualified
    CLOSED_WON,               // Admin/Sales Manager marked deal as Closed Won
    CLOSED_LOST               // Admin/Sales Manager marked deal as Closed Lost
}
