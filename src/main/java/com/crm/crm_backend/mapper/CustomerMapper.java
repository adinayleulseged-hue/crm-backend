package com.crm.crm_backend.mapper;

// mapper/CustomerMapper.java

import com.crm.crm_backend.dto.request.CustomerCreateDTO;
import com.crm.crm_backend.dto.request.CustomerUpdateDTO;
import com.crm.crm_backend.dto.response.CustomerResponseDTO;
import com.crm.crm_backend.model.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")

public interface CustomerMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "territory", ignore = true)
    Customer toEntity(CustomerCreateDTO dto);

    @Mapping(target = "territory", ignore = true)
    void updateEntity(CustomerUpdateDTO dto, @MappingTarget Customer customer);

    @Mapping(target = "territoryId", source = "territory.id")
    CustomerResponseDTO toResponseDTO(Customer customer);
}
