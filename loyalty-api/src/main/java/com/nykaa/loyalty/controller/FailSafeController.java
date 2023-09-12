package com.nykaa.loyalty.controller;

import com.nykaa.base.response.ResponseBean;
import com.nykaa.loyalty.dto.CustomerOfferMappingDto;
import com.nykaa.loyalty.dto.QueryOfferMappingDto;
import com.nykaa.loyalty.entity.LoyaltyCustomerOfferMapping;
import com.nykaa.loyalty.entity.OrderDetails;
import com.nykaa.loyalty.service.OfferRuleDetailsService;
import com.nykaa.loyalty.service.OrderDataService;
import com.nykaa.loyalty.util.EventLogUtil;
import com.nykaa.superstore.loyalty.dto.LoyaltyEventRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("loyaltyFailSafeController")
@RequestMapping("/loyalty/fail-safe")
@RequiredArgsConstructor
@Slf4j
public class FailSafeController {

    private final OfferRuleDetailsService offerRuleDetailsService;

    private final OrderDataService orderDataService;

    @PostMapping("/start-offer")
    public ResponseBean<?> startOffer(@RequestBody LoyaltyEventRequestDto requestDto) {
        try {
            offerRuleDetailsService.startLoyaltyOffer(requestDto);
            return new ResponseBean<>("offer started successfully");
        } catch (Exception e) {
            log.error("Exception occured in start-offer API: {}", e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/end-offer")
    public ResponseBean<?> endOffer(@RequestBody LoyaltyEventRequestDto requestDto) {
        try {
            offerRuleDetailsService.endLoyaltyOffer(requestDto);
            return new ResponseBean<>("offer ended successfully");
        } catch (Exception e) {
            log.error("Exception occured in end-offer API: {}", e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/customer-offer-mapping")
    public ResponseBean<?> customerOfferMapping(@RequestBody CustomerOfferMappingDto requestDto) {
        try {
            offerRuleDetailsService.mapCustomerToOffer(requestDto);
            log.info("offer id {} successfully to customer id {}", requestDto.getOfferId(), requestDto.getCustomerId());
            return new ResponseBean<>("offer mapped to customer successfully");
        } catch (Exception e) {
            log.error("Exception occured in customer-offer-mapping API: {}", e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/execute-query")
    public ResponseBean<?> executeQuery(@RequestBody QueryOfferMappingDto requestDto) {
        try {
            List<Map<String, String>> result = offerRuleDetailsService.executeQuery(requestDto);
            log.info("query result successfully executed");
            return new ResponseBean<>(result);
        } catch (Exception e) {
            log.error("Exception occured in execute-query API: {}", e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/create-order-details")
    public ResponseBean<?> createOrderDetails(@RequestBody OrderDetails requestDto) {
        try {
            OrderDetails order = orderDataService.createOrderDetails(requestDto);
            log.info("order details created successfully for order id {}", requestDto.getOrderId());
            return new ResponseBean<>(order);
        } catch (Exception e) {
            log.error("Exception occured in create-order-details API: {}", e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/update-order-details")
    public ResponseBean<?> updateOrderDetails(@RequestBody OrderDetails requestDto) {
        try {
            OrderDetails order = orderDataService.updateOrderDetails(requestDto);
            log.info("order details updated successfully for order id {}", requestDto.getOrderId());
            return new ResponseBean<>(order);
        } catch (Exception e) {
            log.error("Exception occured in update-order-details API: {}", e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/update-customer-offer-mapping")
    public ResponseBean<?> updateCustomerOfferMapping(@RequestBody LoyaltyCustomerOfferMapping requestDto) {
        try {
            LoyaltyCustomerOfferMapping customerMapping = offerRuleDetailsService
                    .updateCustomerOfferMapping(requestDto);
            log.info("customer offer mapping updated successfully for customer id {} and offer id {}",
                    requestDto.getCustomerId(), requestDto.getOfferId());
            return new ResponseBean<>(customerMapping);
        } catch (Exception e) {
            log.error("Exception occured in update-order-details API: {}", e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }
}
