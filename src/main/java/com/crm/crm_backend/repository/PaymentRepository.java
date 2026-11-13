package com.crm.crm_backend.repository;

import com.crm.crm_backend.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByInvoice_Id(Long invoiceId);
    
    List<Payment> findByInvoice_IdOrderByCreatedAtDesc(Long invoiceId);
    
    List<Payment> findByStatus(String status);
    
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    Optional<Payment> findByTransactionReference(String transactionReference);
    
    List<Payment> findByCustomerId(Long customerId);
    
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    List<Payment> findByReceivedByOrderByCreatedAtDesc(String receivedBy);
}
