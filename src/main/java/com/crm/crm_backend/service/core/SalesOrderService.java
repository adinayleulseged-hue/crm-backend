package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.OrderItemRequestDTO;
import com.crm.crm_backend.dto.request.SalesOrderCreateDTO;
import com.crm.crm_backend.dto.request.SalesOrderUpdateDTO;
import com.crm.crm_backend.dto.response.InvoiceResponseDTO;
import com.crm.crm_backend.dto.response.SalesOrderResponseDTO;
import com.crm.crm_backend.event.publisher.OrderEventPublisher;
import com.crm.crm_backend.exception.OpportunityNotFoundException;
import com.crm.crm_backend.mapper.InvoiceMapper;
import com.crm.crm_backend.mapper.SalesOrderMapper;
import com.crm.crm_backend.model.entity.*;
import com.crm.crm_backend.model.enums.InvoiceStatus;
import com.crm.crm_backend.model.enums.OrderStatus;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.InvoiceRepository;
import com.crm.crm_backend.repository.OpportunityRepository;
import com.crm.crm_backend.repository.SalesOrderRepository;
import com.crm.crm_backend.service.workflow.DomainStatusGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final OrderEventPublisher orderEventPublisher;
    private final com.crm.crm_backend.validator.SalesOrderValidator salesOrderValidator;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public SalesOrderResponseDTO createSalesOrder(SalesOrderCreateDTO dto) {

        salesOrderValidator.validateCreate(dto);

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Opportunity opportunity = null;
        if (dto.getOpportunityId() != null) {
            opportunity = opportunityRepository.findById(dto.getOpportunityId())
                    .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found"));
        }

        SalesOrder order = salesOrderMapper.toEntity(dto);
        if (order.getType() == null) {
            order.setType(dto.getType() != null ? dto.getType() : com.crm.crm_backend.model.enums.OrderType.SALES);
        }

        order.setCustomer(customer);
        order.setOpportunity(opportunity);
        order.setOrderNumber("SO-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.DRAFT);

        BigDecimal subtotal = BigDecimal.ZERO;

        if (dto.getItems() != null) {

            for (OrderItemRequestDTO itemDTO : dto.getItems()) {

                int qty = itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 1;
                BigDecimal unit = itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal totalPrice = unit.multiply(BigDecimal.valueOf(qty));

                OrderItem item = OrderItem.builder()
                        .itemName(itemDTO.getItemName())
                        .sku(itemDTO.getSku())
                        .description(itemDTO.getDescription())
                        .quantity(qty)
                        .unitPrice(unit)
                        .totalPrice(totalPrice)
                        .salesOrder(order)
                        .build();

                subtotal = subtotal.add(totalPrice);
                order.getOrderItems().add(item);
            }
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);

        SalesOrder saved = salesOrderRepository.save(order);

        return salesOrderMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponseDTO getSalesOrder(Long id) {

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sales Order not found"));

        return salesOrderMapper.toResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<SalesOrderResponseDTO> getAllSalesOrders(Pageable pageable) {

        return salesOrderRepository.findAll(pageable)
                .map(salesOrderMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<SalesOrderResponseDTO> getAllSalesOrders(Long customerId, Pageable pageable) {
        if (customerId != null) {
            return salesOrderRepository.findByCustomerId(customerId, pageable)
                    .map(salesOrderMapper::toResponseDTO);
        }
        return salesOrderRepository.findAll(pageable)
                .map(salesOrderMapper::toResponseDTO);
    }

    public SalesOrderResponseDTO updateSalesOrder(
            Long id,
            SalesOrderUpdateDTO dto) {

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sales Order not found"));

        if (dto.getStatus() != null) {
            DomainStatusGuard.assertOrderTransition(order.getStatus(), dto.getStatus());
        }

        salesOrderMapper.updateEntityFromDTO(dto, order);

        SalesOrder updated = salesOrderRepository.save(order);

        return salesOrderMapper.toResponseDTO(updated);
    }

    public SalesOrderResponseDTO confirmSalesOrder(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Order not found"));

        if (order.getStatus() == OrderStatus.APPROVED
                || order.getStatus() == OrderStatus.PROCESSING
                || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Sales order already confirmed (status=" + order.getStatus() + ") — cannot confirm twice");
        }

        order.getOrderItems().size();
        salesOrderValidator.validateCanConfirm(order);
        DomainStatusGuard.assertOrderTransition(order.getStatus(), OrderStatus.APPROVED);

        order.setStatus(OrderStatus.APPROVED);
        SalesOrder confirmed = salesOrderRepository.save(order);
        orderEventPublisher.publishSalesOrderConfirmed(confirmed);
        log.info("Sales order {} confirmed", confirmed.getOrderNumber());

        return salesOrderMapper.toResponseDTO(confirmed);
    }

    public void deleteSalesOrder(Long id) {

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sales Order not found"));

        if (order.getStatus() == OrderStatus.APPROVED
                || order.getStatus() == OrderStatus.PROCESSING
                || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot delete a sales order in status " + order.getStatus());
        }

        salesOrderRepository.delete(order);

        log.info("Sales Order {} deleted", id);
    }

    /**
     * Generate invoice from sales order
     * SalesOrder -> Invoice (workflow transition)
     */
    public InvoiceResponseDTO generateInvoiceFromOrder(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new RuntimeException("Sales Order not found with id: " + salesOrderId));

        // Validate that order is in appropriate status
        if (order.getStatus() != OrderStatus.APPROVED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Sales order must be APPROVED or PROCESSING to generate invoice. Current status: " + order.getStatus());
        }

        // Check if invoice already generated
        if (order.getInvoiceGenerated() != null && order.getInvoiceGenerated()) {
            throw new IllegalStateException("Invoice already generated for this sales order");
        }

        // Create invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + System.currentTimeMillis())
                .salesOrder(order)
                .customer(order.getCustomer())
                .status(InvoiceStatus.PENDING)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))  // 30 days payment term
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO)
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                .totalAmount(order.getTotalAmount())
                .paidAmount(BigDecimal.ZERO)
                .balanceAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .notes("Generated from Sales Order: " + order.getOrderNumber())
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Mark sales order as invoice generated
        order.setInvoiceGenerated(true);
        salesOrderRepository.save(order);

        log.info("Invoice {} generated from Sales Order {}", 
                savedInvoice.getInvoiceNumber(), order.getOrderNumber());

        return invoiceMapper.toResponseDTO(savedInvoice);
    }
}
