package com.crm.crm_backend.repository;


import com.crm.crm_backend.model.entity.Invoice;
import com.crm.crm_backend.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    long count();
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findBySalesOrderId(Long salesOrderId);
}
