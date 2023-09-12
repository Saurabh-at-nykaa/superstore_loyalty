package com.nykaa.loyalty.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserOfferDetails {

    private Long offerId;

    private String offerName;

    private List<TierDetails> tiers;

    private double potentialProgress;

    private double potentialSupercashEarning;

    private int tierPassed;

    private double maxRewardLimit;

    private String nextOfferMessage;

    private String offerStartDate;

    private String offerEndDate;

    private String offerTitle;

    private String offerSubtitle;

}
