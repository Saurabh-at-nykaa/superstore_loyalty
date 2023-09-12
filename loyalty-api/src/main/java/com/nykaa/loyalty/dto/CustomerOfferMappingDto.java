package com.nykaa.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOfferMappingDto {

    private Long offerId;
    private String customerId;
    private String previousCycleResult;

}
