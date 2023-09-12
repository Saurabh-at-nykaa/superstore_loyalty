package com.nykaa.loyalty.jms.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nykaa.loyalty.service.OfferRuleDetailsService;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.superstore.loyalty.dto.NewUserEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventListener {

    @Autowired
    @Qualifier("loyaltyObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private OfferRuleDetailsService offerRuleDetailsService;

    @JmsListener(destination = Constants.Queues.SUPERSTORE_NEW_USER_EVENT_QUEUE)
    public void processNewUserEvent(String message) {
        log.info("New kyc approved user event details received : {}", message);
        try {
            NewUserEventDTO newUserEventDTO = objectMapper.readValue(message, NewUserEventDTO.class);
            offerRuleDetailsService.mapNewUserToOffer(newUserEventDTO);
        } catch (Exception e) {
            log.error("Issue in parsing new user event details string - {} with exception {}", message, e.getMessage());
            // todo need to track the failed cases for now tracking using kibana alerts
        }
    }
}
