package com.nykaa.loyalty.dto;

import lombok.Data;

@Data
public class TierDetails {

    private double target;

    private double reward;
    
    private String rewardType;

}
