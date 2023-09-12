package com.nykaa.loyalty.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.service.EventSchedulerService;
import com.nykaa.loyalty.service.OfferRuleDetailsService;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.DateUtil;
import com.nykaa.superstore.loyalty.dto.LoyaltyEventRequestDto;
import com.nykaa.superstore.loyalty.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JMSListener {

    @Autowired
    @Qualifier("loyaltyObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private OfferRuleDetailsService offerRuleDetailsService;

    @Autowired
    private EventSchedulerService eventSchedulerService;

    @JmsListener(destination = Constants.Queues.SUPERSTORE_LOYALTY_CREATE_OFFER, containerFactory = "jmsListenerContainerFactory")
    public void processLoyaltyOfferStartEvent(String inputJson) throws JsonProcessingException {
        log.info("Event received loyalty offer : {}", inputJson);
        try {
            processLoyaltyOfferEvent(inputJson);
        } catch (Exception e) {
            log.error("Error in parsing offer scheduler event : {}", inputJson);
        }
    }

    private void processLoyaltyOfferEvent(String inputJson) throws JsonProcessingException {
        boolean isSuccess = false;
            LoyaltyEventRequestDto loyaltyEventRequestDto = objectMapper
                    .setDateFormat(DateUtil.getEventSchedularParser()).readValue(inputJson, LoyaltyEventRequestDto.class);
            try {
                if (EventType.OFFER_START.equals(loyaltyEventRequestDto.getEventType())) {
                    offerRuleDetailsService.startLoyaltyOffer(loyaltyEventRequestDto);
                } else if (EventType.OFFER_END.equals(loyaltyEventRequestDto.getEventType())) {
                    offerRuleDetailsService.endLoyaltyOffer(loyaltyEventRequestDto);
                }
                isSuccess = true;
            } catch (Exception e) {
                log.error("Error while mapping offer to users : {}, with exception : {}", e.getMessage(), e);
                throw new LoyaltyException(ErrorCodes.OFFER_CUSTOMER_MAPPING_ERROR);
            } finally {
                if (!isSuccess) {
                    loyaltyEventRequestDto.setCounter(loyaltyEventRequestDto.getCounter() + 1);
                    loyaltyEventRequestDto.setTriggerFireTime(
                            DateUtils.addMinutes(loyaltyEventRequestDto.getTriggerFireTime(), loyaltyEventRequestDto.getCounter()));
                    eventSchedulerService.scheduleLoyaltyOfferEvent(loyaltyEventRequestDto);
                }
            }

    }
}
