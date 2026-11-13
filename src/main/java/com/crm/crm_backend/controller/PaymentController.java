package com.crm.crm_backend.controller;

import com.crm.crm_backend.model.entity.Payment;
import com.crm.crm_backend.model.entity.Invoice;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.enums.PaymentMethod;
import com.crm.crm_backend.model.enums.PaymentStatus;
import com.crm.crm_backend.model.enums.InvoiceStatus;
import com.crm.crm_backend.repository.PaymentRepository;
import com.crm.crm_backend.repository.InvoiceRepository;
import com.crm.crm_backend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Create a new payment
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> paymentData) {
        try {
            Payment payment = new Payment();
            
            // Basic fields - handle invoiceId or invoiceNumber
            Invoice invoice = null;
            Long invoiceIdValue = null;
            String invoiceNumberValue = null;
            
            if (paymentData.containsKey("invoiceId")) {
                Object invoiceIdObj = paymentData.get("invoiceId");
                if (invoiceIdObj != null) {
                    try {
                        invoiceIdValue = Long.valueOf(invoiceIdObj.toString());
                        invoice = invoiceRepository.findById(invoiceIdValue).orElse(null);
                    } catch (NumberFormatException e) {
                        // Maybe it's an invoice number string
                        invoiceNumberValue = invoiceIdObj.toString();
                        invoice = invoiceRepository.findByInvoiceNumber(invoiceNumberValue).orElse(null);
                    }
                }
            }
            
            // Try invoice number if ID didn't work
            if (invoice == null && paymentData.containsKey("invoiceNumber")) {
                invoiceNumberValue = paymentData.get("invoiceNumber").toString();
                invoice = invoiceRepository.findByInvoiceNumber(invoiceNumberValue).orElse(null);
            }
            
            // If invoice not found, we can still create the payment record
            // This handles cases where frontend has mock/cached invoices
            if (invoice == null) {
                System.out.println("⚠️ Warning: Invoice not found in database. Invoice ID/Number: " + 
                    (invoiceIdValue != null ? invoiceIdValue : invoiceNumberValue));
                System.out.println("Creating payment record without invoice reference...");
            } else {
                payment.setInvoice(invoice);
                
                // Set customer from invoice
                if (invoice.getCustomer() != null) {
                    payment.setCustomer(invoice.getCustomer());
                }

                // Update invoice status to CONFIRMED (Payment submitted by customer, awaiting finance approval)
                invoice.setStatus(InvoiceStatus.CONFIRMED);
                invoiceRepository.save(invoice);
                System.out.println("✓ Invoice " + invoice.getInvoiceNumber() + " status updated to CONFIRMED (Pending Finance Approval)");
            }
            
            // If no customer yet, try to get from customerId in payload
            if (payment.getCustomer() == null && paymentData.containsKey("customerId")) {
                try {
                    Long customerId = Long.valueOf(paymentData.get("customerId").toString());
                    Customer customer = customerRepository.findById(customerId).orElse(null);
                    if (customer != null) {
                        payment.setCustomer(customer);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            // Set amount
            payment.setAmount(new BigDecimal(paymentData.get("amount").toString()));
            
            // Payment method
            String methodStr = paymentData.get("paymentMethod").toString();
            try {
                payment.setPaymentMethod(PaymentMethod.valueOf(methodStr));
            } catch (IllegalArgumentException e) {
                // If payment method not found, default to CASH
                payment.setPaymentMethod(PaymentMethod.CASH);
            }
            
            // Transaction reference
            if (paymentData.containsKey("transactionId")) {
                payment.setTransactionReference(paymentData.get("transactionId").toString());
            }
            
            // Payment date
            if (paymentData.containsKey("paymentDate")) {
                try {
                    payment.setPaymentDate(LocalDate.parse(paymentData.get("paymentDate").toString()));
                } catch (Exception e) {
                    payment.setPaymentDate(LocalDate.now());
                }
            } else {
                payment.setPaymentDate(LocalDate.now());
            }
            
            // Received by (for tracking)
            if (paymentData.containsKey("receivedBy")) {
                payment.setReceivedBy(paymentData.get("receivedBy").toString());
            } else if (paymentData.containsKey("cashReceivedBy")) {
                payment.setReceivedBy(paymentData.get("cashReceivedBy").toString());
            }
            
            // Notes - include invoice reference if invoice not found
            StringBuilder notes = new StringBuilder();
            if (paymentData.containsKey("notes") && paymentData.get("notes") != null) {
                notes.append(paymentData.get("notes").toString());
            }
            if (invoice == null && invoiceNumberValue != null) {
                if (notes.length() > 0) notes.append(" | ");
                notes.append("Reference Invoice: ").append(invoiceNumberValue);
            }
            payment.setNotes(notes.toString());
            
            // Set status as pending
            payment.setStatus(PaymentStatus.PENDING);
            
            // Generate payment number
            String paymentNumber = "PAY-" + System.currentTimeMillis();
            payment.setPaymentNumber(paymentNumber);
            
            // Save payment
            Payment savedPayment = paymentRepository.save(payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment created successfully");
            response.put("data", savedPayment);
            if (invoice == null) {
                response.put("warning", "Invoice not found in database but payment was recorded");
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error creating payment: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments(
            @RequestParam(required = false) Long invoiceId,
            @RequestParam(required = false) Long customerId) {
        try {
            if (invoiceId != null) {
                return ResponseEntity.ok(paymentRepository.findByInvoice_Id(invoiceId));
            }
            if (customerId != null) {
                return ResponseEntity.ok(paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId));
            }
            return ResponseEntity.ok(paymentRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return paymentRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Find payment by transaction reference
     */
    @GetMapping("/by-transaction/{transactionRef}")
    public ResponseEntity<Payment> findByTransactionReference(@PathVariable String transactionRef) {
        return paymentRepository.findByTransactionReference(transactionRef)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Approve/Verify payment (for finance officers)
     * When approved, payment status becomes COMPLETED and invoice becomes PAID
     */
    @PatchMapping("/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@PathVariable Long id) {
        return paymentRepository.findById(id)
            .map(payment -> {
                // Update payment status
                payment.setStatus(PaymentStatus.COMPLETED);
                Payment savedPayment = paymentRepository.save(payment);
                
                // Update invoice status to PAID if invoice exists
                if (payment.getInvoice() != null) {
                    Invoice invoice = payment.getInvoice();
                    invoice.setStatus(InvoiceStatus.PAID);
                    invoice.setPaidAmount(payment.getAmount());
                    invoice.setBalanceAmount(invoice.getTotalAmount().subtract(payment.getAmount()));
                    invoice.setPaidAt(java.time.LocalDateTime.now());
                    invoiceRepository.save(invoice);
                    
                    System.out.println("✓ Payment approved: " + payment.getPaymentNumber());
                    System.out.println("✓ Invoice updated to PAID: " + invoice.getInvoiceNumber());
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment approved successfully");
                response.put("data", savedPayment);
                
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Reject payment (for finance officers)
     * When rejected, payment status becomes FAILED
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectPayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return paymentRepository.findById(id)
            .map(payment -> {
                // Update payment status
                payment.setStatus(PaymentStatus.FAILED);
                
                // Add rejection reason to notes
                if (body.containsKey("reason")) {
                    String existingNotes = payment.getNotes() != null ? payment.getNotes() : "";
                    payment.setNotes(existingNotes + "\n[REJECTED] Reason: " + body.get("reason"));
                }
                
                Payment savedPayment = paymentRepository.save(payment);
                
                System.out.println("✗ Payment rejected: " + payment.getPaymentNumber());
                if (body.containsKey("reason")) {
                    System.out.println("  Reason: " + body.get("reason"));
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment rejected");
                response.put("data", savedPayment);
                
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
