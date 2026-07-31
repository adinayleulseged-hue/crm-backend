package com.crm.crm_backend.mapper;


import com.crm.crm_backend.dto.request.QuotationCreateDTO;
import com.crm.crm_backend.dto.request.QuotationUpdateDTO;
import com.crm.crm_backend.dto.response.QuotationResponseDTO;
import com.crm.crm_backend.model.entity.Quotation;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface QuotationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quotationNumber", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "opportunity", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Quotation toEntity(QuotationCreateDTO dto);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "opportunityId", source = "opportunity.id")
    QuotationResponseDTO toResponseDTO(Quotation quotation);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quotationNumber", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "opportunity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(
            QuotationUpdateDTO dto,
            @MappingTarget Quotation quotation
    );
}
