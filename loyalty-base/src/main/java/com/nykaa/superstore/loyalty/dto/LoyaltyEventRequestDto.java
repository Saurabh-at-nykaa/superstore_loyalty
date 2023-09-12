package com.nykaa.superstore.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import com.nykaa.superstore.loyalty.enums.EventType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoyaltyEventRequestDto {

    private Long offerId;

    private EventType eventType;

    // Start - scheduled job related fields
    private Integer counter;

    private Date triggerFireTime;
    // End - scheduled job related fields
}
