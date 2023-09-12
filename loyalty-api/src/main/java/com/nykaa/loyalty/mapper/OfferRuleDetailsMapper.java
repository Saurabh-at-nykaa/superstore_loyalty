package com.nykaa.loyalty.mapper;

import com.nykaa.loyalty.dto.OfferRuleDetailsDto;
import com.nykaa.loyalty.entity.OfferRuleDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfferRuleDetailsMapper {

    @Mapping(target="customerType", source="userSegmentQuery.customerType")
    OfferRuleDetails dtoToEntity(OfferRuleDetailsDto dto);

    @Mapping(source="customerType", target="userSegmentQuery.customerType")
    OfferRuleDetailsDto entityToDto(OfferRuleDetails entity);

}
