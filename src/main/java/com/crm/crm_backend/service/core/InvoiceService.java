package com.crm.crm_backend.service.core;


import com.crm.crm_backend.dto.request.InvoiceCreateDTO;
import com.crm.crm_backend.dto.request.InvoiceUpdateDTO;
import com.crm.crm_backend.dto.response.InvoiceResponseDTO;
import com.crm.crm_backend.mapper.InvoiceMapper;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.entity.Invoice;
import com.crm.crm_backend.model.entity.SalesOrder;
import com.crm.crm_backend.model.enums.InvoiceStatus;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.InvoiceRepository;
import com.crm.crm_backend.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceResponseDTO createInvoice(InvoiceCreateDTO dto) {

        SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId())
                .orElseThrow(() -> new RuntimeException("Sales Order not found"));

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Invoice invoice = invoiceMapper.toEntity(dto);

        invoice.setSalesOrder(order);
        invoice.setCustomer(customer);

        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());

        invoice.setStatus(InvoiceStatus.DRAFT);

        invoice.setSubtotal(order.getSubtotal());
        invoice.setTaxAmount(dto.getTaxAmount() == null ? BigDecimal.ZERO : dto.getTaxAmount());
        invoice.setDiscountAmount(dto.getDiscountAmount() == null ? BigDecimal.ZERO : dto.getDiscountAmount());

        BigDecimal total =
                invoice.getSubtotal()
                        .add(invoice.getTaxAmount())
                        .subtract(invoice.getDiscountAmount());

        invoice.setTotalAmount(total);

        invoice.setPaidAmount(BigDecimal.ZERO);

        invoice.setBalanceAmount(total);

        Invoice saved = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoice(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        return invoiceMapper.toResponseDTO(invoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponseDTO> getAllInvoices(Pageable pageable) {

        return invoiceRepository.findAll(pageable)
                .map(invoiceMapper::toResponseDTO);
    }

    public InvoiceResponseDTO updateInvoice(Long id,
                                            InvoiceUpdateDTO dto) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoiceMapper.updateEntityFromDTO(dto, invoice);

        if (invoice.getPaidAmount() == null) {
            invoice.setPaidAmount(BigDecimal.ZERO);
        }

        invoice.setBalanceAmount(
                invoice.getTotalAmount().subtract(invoice.getPaidAmount()));

        if (invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        }

        Invoice updated = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(updated);
    }

    public void deleteInvoice(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoiceRepository.delete(invoice);

        log.info("Invoice {} deleted", id);
    }

}