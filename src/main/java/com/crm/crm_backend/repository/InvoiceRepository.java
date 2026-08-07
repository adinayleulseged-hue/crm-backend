package com.crm.crm_backend.repository;


import com.crm.crm_backend.model.entity.Invoice;
import com.crm.crm_backend.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    long countByStatus(InvoiceStatus status);

    @Query("select coalesce(sum(i.totalAmount), 0) from Invoice i where i.status <> com.crm.crm_backend.model.enums.InvoiceStatus.CANCELLED")
    BigDecimal sumTotalAmountExcludingCancelled();

    @Query("select coalesce(sum(i.paidAmount), 0) from Invoice i where i.status <> com.crm.crm_backend.model.enums.InvoiceStatus.CANCELLED")
    BigDecimal sumPaidAmountExcludingCancelled();
}
