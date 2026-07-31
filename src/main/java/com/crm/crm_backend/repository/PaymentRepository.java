package com.crm.crm_backend.repository;


import com.crm.crm_backend.model.entity.Payment;
import com.crm.crm_backend.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    long count();
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    boolean existsByPaymentNumber(String paymentNumber);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByCustomerId(Long customerId);
}
