package com.nykaa.loyalty.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserLoyaltyDetailsDto {
    private OfferRuleDetailsDto appliedLoyaltyOfferDto;
    private List<Long> targets;
    private String currentProgress;
    private List<Long> rewards;
}
