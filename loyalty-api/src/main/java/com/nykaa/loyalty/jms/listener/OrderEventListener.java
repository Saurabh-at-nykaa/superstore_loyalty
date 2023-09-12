package com.nykaa.loyalty.jms.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nykaa.base.dto.order.OrderUpdatesInfo;
import com.nykaa.loyalty.enums.OrderStatus;
import com.nykaa.loyalty.service.OrderDataService;
import com.nykaa.loyalty.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class OrderEventListener {

    @Autowired
    @Qualifier("loyaltyObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private OrderDataService orderDataService;

    @JmsListener(destination = Constants.Queues.SUPERSTORE_LOYALTY_ORDER_UPDATE_QUEUE)
    public void orderUpdateReceived(String message) throws Exception {
        try {
            log.info("Processing message received on SuperstoreLoyaltyOrderUpdateQueue - {}", message);
            OrderUpdatesInfo orderUpdateInfo = objectMapper.readValue(message, OrderUpdatesInfo.class);
            if (Objects.isNull(OrderStatus.valueOfStatus(orderUpdateInfo.getStatus()))
                    && Objects.isNull(OrderStatus.valueOfName(orderUpdateInfo.getStatus()))) {
                log.info("Update received for order id {} with order status {}. Processing not required",
                        orderUpdateInfo.getOrderNo(), orderUpdateInfo.getStatus());
                return;
            }
            orderDataService.processOrderDataEvent(orderUpdateInfo);
        } catch (Exception e) {
            log.error("Issue in parsing order update event string - {} with exception {}", message, e.getMessage());
            // todo need to track the failed cases for now tracking using kibana alerts
        }
    }
}
