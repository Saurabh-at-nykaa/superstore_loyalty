package com.nykaa.loyalty.service;

import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.superstore.loyalty.dto.LoyaltyEventRequestDto;

public interface EventSchedulerService {

       void scheduleLoyaltyOfferEvent(LoyaltyEventRequestDto requestDto) throws LoyaltyException;
}
