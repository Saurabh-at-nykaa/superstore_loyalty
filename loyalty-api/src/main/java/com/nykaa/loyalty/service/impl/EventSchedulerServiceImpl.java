package com.nykaa.loyalty.service.impl;

import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.jms.sender.JMSSender;
import com.nykaa.loyalty.service.EventSchedulerService;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.SystemPropertyUtil;
import com.nykaa.superstore.loyalty.dto.LoyaltyEventRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class EventSchedulerServiceImpl implements EventSchedulerService {

    @Autowired
    private JMSSender jmsSender;

    @Value("${eventSchedulerQueueUrl}")
    private String eventSchedulerQueueUrl;

    @Override
    public void scheduleLoyaltyOfferEvent(LoyaltyEventRequestDto requestDto) throws LoyaltyException {
        if (requestDto == null) {
            throw new LoyaltyException(ErrorCodes.NULL_MESSAGE);
        }

        log.info("Scheduling loyalty event job for request: {}, counter: {}", requestDto, requestDto.getCounter());

        int retryLimit = Integer.parseInt(SystemPropertyUtil.getProperty(Constants.CacheKeys.RETRY_LIMIT,
                Constants.CacheKeys.DEFAULT_LOYALTY_START_OFFER_RETRY));

        if (retryLimit >= requestDto.getCounter()) {
            Long offerId = requestDto.getOfferId();

            try {
                requestDto.setTriggerFireTime(DateUtils.addMinutes(requestDto.getTriggerFireTime(),
                            requestDto.getCounter()));

                HashMap<String, Object> message = new HashMap<>();
                message.put(Constants.EventSchedulerParam.EVENT_NAME, Constants.EventSchedulerParam.LOYALTY_START_EVENT);
                message.put(Constants.EventSchedulerParam.EVENT_DATA, requestDto);

                jmsSender.sendMessage(message, eventSchedulerQueueUrl, false);

                log.info("Successfully scheduled loyalty start job for offer id: {}, at retry: {} with job message: {}",
                        offerId, requestDto.getCounter(), message);
            } catch (Exception e) {
                log.error("Exception occurred while scheduling loyalty start job for offer id: {}, counter: {}," +
                        " exception: {}", offerId, requestDto.getCounter(), e);
                throw new LoyaltyException(ErrorCodes.EVENT_SCHEDULER_ERROR);
            }
        }
    }
}
