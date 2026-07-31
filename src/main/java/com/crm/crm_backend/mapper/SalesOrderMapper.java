package com.crm.crm_backend.mapper;


import com.crm.crm_backend.dto.request.SalesOrderCreateDTO;
import com.crm.crm_backend.dto.request.SalesOrderUpdateDTO;
import com.crm.crm_backend.dto.response.SalesOrderResponseDTO;
import com.crm.crm_backend.model.entity.SalesOrder;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SalesOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "opportunity", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SalesOrder toEntity(SalesOrderCreateDTO dto);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "opportunityId", source = "opportunity.id")
    @Mapping(target = "items", ignore = true)
    SalesOrderResponseDTO toResponseDTO(SalesOrder entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "opportunity", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(
            SalesOrderUpdateDTO dto,
            @MappingTarget SalesOrder entity
    );
}
