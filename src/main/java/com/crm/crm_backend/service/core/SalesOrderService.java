package com.crm.crm_backend.service.core;


import com.crm.crm_backend.dto.request.OrderItemRequestDTO;
import com.crm.crm_backend.dto.request.SalesOrderCreateDTO;
import com.crm.crm_backend.dto.request.SalesOrderUpdateDTO;
import com.crm.crm_backend.dto.response.SalesOrderResponseDTO;
import com.crm.crm_backend.exception.OpportunityNotFoundException;
import com.crm.crm_backend.mapper.SalesOrderMapper;
import com.crm.crm_backend.model.entity.Customer;
import com.crm.crm_backend.model.entity.Opportunity;
import com.crm.crm_backend.model.entity.OrderItem;
import com.crm.crm_backend.model.entity.SalesOrder;
import com.crm.crm_backend.model.enums.OrderStatus;
import com.crm.crm_backend.repository.CustomerRepository;
import com.crm.crm_backend.repository.OpportunityRepository;
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
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final SalesOrderMapper salesOrderMapper;

    public SalesOrderResponseDTO createSalesOrder(SalesOrderCreateDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId())
                .orElseThrow(() -> new OpportunityNotFoundException("Opportunity not found"));

        SalesOrder order = salesOrderMapper.toEntity(dto);

        order.setCustomer(customer);
        order.setOpportunity(opportunity);
        order.setOrderNumber("SO-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.DRAFT);

        BigDecimal subtotal = BigDecimal.ZERO;

        if (dto.getItems() != null) {

            for (OrderItemRequestDTO itemDTO : dto.getItems()) {

                OrderItem item = OrderItem.builder()
                        .itemName(itemDTO.getItemName())
                        .description(itemDTO.getDescription())
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .salesOrder(order)
                        .build();

                BigDecimal totalPrice =
                        itemDTO.getUnitPrice().multiply(
                                BigDecimal.valueOf(itemDTO.getQuantity()));

                item.setTotalPrice(totalPrice);

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

    public SalesOrderResponseDTO updateSalesOrder(
            Long id,
            SalesOrderUpdateDTO dto) {

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sales Order not found"));

        salesOrderMapper.updateEntityFromDTO(dto, order);

        SalesOrder updated = salesOrderRepository.save(order);

        return salesOrderMapper.toResponseDTO(updated);
    }

    public void deleteSalesOrder(Long id) {

        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sales Order not found"));

        salesOrderRepository.delete(order);

        log.info("Sales Order {} deleted", id);
    }

}