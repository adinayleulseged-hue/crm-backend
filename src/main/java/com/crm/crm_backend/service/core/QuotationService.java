package com.crm.crm_backend.service.core;

import com.crm.crm_backend.dto.request.QuotationCreateDTO;
import com.crm.crm_backend.dto.request.QuotationItemRequestDTO;
import com.crm.crm_backend.dto.request.QuotationUpdateDTO;
import com.crm.crm_backend.dto.response.QuotationResponseDTO;
import com.crm.crm_backend.dto.response.SalesOrderResponseDTO;
import com.crm.crm_backend.exception.QuotationNotFoundException;
import com.crm.crm_backend.mapper.QuotationMapper;
import com.crm.crm_backend.mapper.SalesOrderMapper;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.entity.Opportunity;
import com.crm.crm_backend.model.entity.OrderItem;
import com.crm.crm_backend.model.entity.Quotation;
import com.crm.crm_backend.model.entity.QuotationItem;
import com.crm.crm_backend.model.entity.SalesOrder;
import com.crm.crm_backend.model.enums.OrderStatus;
import com.crm.crm_backend.model.enums.OrderType;
import com.crm.crm_backend.model.enums.QuotationStatus;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.OpportunityRepository;
import com.crm.crm_backend.repository.QuotationRepository;
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
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final QuotationMapper quotationMapper;
    private final SalesOrderMapper salesOrderMapper;

    public QuotationResponseDTO createQuotation(QuotationCreateDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId())
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));

        Quotation quotation = quotationMapper.toEntity(dto);

        quotation.setCustomer(customer);
        quotation.setOpportunity(opportunity);
        quotation.setQuotationNumber("QT-" + System.currentTimeMillis());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setActive(true);

        applyItemsAndTotals(quotation, dto);

        Quotation saved = quotationRepository.save(quotation);

        log.info("Quotation {} created", saved.getQuotationNumber());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public QuotationResponseDTO getQuotation(Long id) {

        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() ->
                        new QuotationNotFoundException("Quotation not found: " + id));

        return toResponse(quotation);
    }

    @Transactional(readOnly = true)
    public Page<QuotationResponseDTO> getAllQuotations(Pageable pageable) {

        return quotationRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public QuotationResponseDTO updateQuotation(Long id,
                                                QuotationUpdateDTO dto) {

        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() ->
                        new QuotationNotFoundException("Quotation not found: " + id));

        if (dto.getStatus() != null) {
            DomainStatusGuard.assertQuotationTransition(quotation.getStatus(), dto.getStatus());
        }

        quotationMapper.updateEntityFromDTO(dto, quotation);

        Quotation updated = quotationRepository.save(quotation);

        return toResponse(updated);
    }

    public QuotationResponseDTO changeStatus(Long id, QuotationStatus newStatus) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new QuotationNotFoundException("Quotation not found: " + id));

        DomainStatusGuard.assertQuotationTransition(quotation.getStatus(), newStatus);
        quotation.setStatus(newStatus);
        return toResponse(quotationRepository.save(quotation));
    }

    /**
     * Accept quotation and create a linked sales order, copying line items (or a
     * single summary line when the quotation has header totals only).
     */
    public SalesOrderResponseDTO acceptQuotation(Long id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new QuotationNotFoundException("Quotation not found: " + id));

        if (salesOrderRepository.existsByQuotationId(quotation.getId())) {
            throw new IllegalStateException(
                    "Quotation already accepted and linked to a sales order");
        }

        DomainStatusGuard.assertQuotationTransition(quotation.getStatus(), QuotationStatus.ACCEPTED);
        quotation.setStatus(QuotationStatus.ACCEPTED);
        quotation.getItems().size();
        quotationRepository.save(quotation);

        SalesOrder order = SalesOrder.builder()
                .orderNumber("SO-" + System.currentTimeMillis())
                .customer(quotation.getCustomer())
                .opportunity(quotation.getOpportunity())
                .quotation(quotation)
                .status(OrderStatus.DRAFT)
                .type(OrderType.SALES)
                .orderDate(LocalDate.now())
                .currency("ETB")
                .notes("Created from quotation " + quotation.getQuotationNumber())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        if (quotation.getItems() != null && !quotation.getItems().isEmpty()) {
            for (QuotationItem qi : quotation.getItems()) {
                OrderItem item = OrderItem.builder()
                        .itemName(qi.getItemName())
                        .sku(qi.getSku())
                        .description(qi.getDescription())
                        .quantity(qi.getQuantity())
                        .unitPrice(qi.getUnitPrice())
                        .totalPrice(qi.getTotalPrice())
                        .salesOrder(order)
                        .build();
                order.getOrderItems().add(item);
                subtotal = subtotal.add(qi.getTotalPrice() != null ? qi.getTotalPrice() : BigDecimal.ZERO);
            }
        } else {
            BigDecimal amount = quotation.getTotalAmount() != null
                    ? quotation.getTotalAmount()
                    : (quotation.getSubtotal() != null ? quotation.getSubtotal() : BigDecimal.ZERO);
            OrderItem item = OrderItem.builder()
                    .itemName("Quoted products - " + quotation.getQuotationNumber())
                    .sku("QT-" + quotation.getId())
                    .description(quotation.getNotes())
                    .quantity(1)
                    .unitPrice(amount)
                    .totalPrice(amount)
                    .salesOrder(order)
                    .build();
            order.getOrderItems().add(item);
            subtotal = amount;
        }

        BigDecimal discount = quotation.getDiscount() != null ? quotation.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = quotation.getTax() != null ? quotation.getTax() : BigDecimal.ZERO;

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setTaxAmount(tax);
        order.setTotalAmount(subtotal.add(tax).subtract(discount));

        SalesOrder saved = salesOrderRepository.save(order);
        log.info("Quotation {} accepted → sales order {}",
                quotation.getQuotationNumber(), saved.getOrderNumber());

        return salesOrderMapper.toResponseDTO(saved);
    }

    public void deleteQuotation(Long id) {

        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() ->
                        new QuotationNotFoundException("Quotation not found: " + id));

        if (quotation.getStatus() == QuotationStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot delete an accepted quotation");
        }

        quotationRepository.delete(quotation);

        log.info("Quotation {} deleted", id);
    }

    private void applyItemsAndTotals(Quotation quotation, QuotationCreateDTO dto) {
        BigDecimal discount = dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = dto.getTax() != null ? dto.getTax() : BigDecimal.ZERO;

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (QuotationItemRequestDTO itemDto : dto.getItems()) {
                int qty = itemDto.getQuantity() != null ? itemDto.getQuantity() : 1;
                BigDecimal unit = itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));

                QuotationItem item = QuotationItem.builder()
                        .itemName(itemDto.getItemName() != null ? itemDto.getItemName() : "Item")
                        .sku(itemDto.getSku())
                        .description(itemDto.getDescription())
                        .quantity(qty)
                        .unitPrice(unit)
                        .totalPrice(lineTotal)
                        .quotation(quotation)
                        .build();
                quotation.getItems().add(item);
                subtotal = subtotal.add(lineTotal);
            }
            quotation.setSubtotal(subtotal);
            quotation.setDiscount(discount);
            quotation.setTax(tax);
            quotation.setTotalAmount(subtotal.add(tax).subtract(discount));
        } else {
            BigDecimal subtotal = dto.getSubtotal() != null ? dto.getSubtotal() : BigDecimal.ZERO;
            quotation.setSubtotal(subtotal);
            quotation.setDiscount(discount);
            quotation.setTax(tax);
            quotation.setTotalAmount(dto.getTotalAmount() != null
                    ? dto.getTotalAmount()
                    : subtotal.add(tax).subtract(discount));
        }
    }

    private QuotationResponseDTO toResponse(Quotation quotation) {
        QuotationResponseDTO dto = quotationMapper.toResponseDTO(quotation);
        salesOrderRepository.findByQuotationId(quotation.getId())
                .ifPresent(order -> dto.setSalesOrderId(order.getId()));
        return dto;
    }
}
