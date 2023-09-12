package com.nykaa.loyalty.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.nykaa.base.dto.order.OrderData;
import com.nykaa.base.dto.order.OrderUpdatesInfo;
import com.nykaa.base.enums.ServiceName;
import com.nykaa.loyalty.entity.OrderDetails;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.enums.OrderStatus;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.repository.master.OrderDetailsRepository;
import com.nykaa.loyalty.repository.read.OrderDetailsReadRepository;
import com.nykaa.loyalty.service.OfferRuleDetailsService;
import com.nykaa.loyalty.service.OrderDataService;
import com.nykaa.loyalty.service.helper.RestServiceHelper;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.DateUtil;
import com.nykaa.loyalty.util.SystemPropertyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderDataServiceImpl implements OrderDataService {

    private final OrderDetailsRepository orderDetailsRepository;

    private final OrderDetailsReadRepository orderDetailsReadRepository;

    private final OfferRuleDetailsService offerRuleDetailsService;

    private final RestServiceHelper restServiceHelper;

    @Override
    public void processOrderDataEvent(OrderUpdatesInfo orderUpdateInfo) {
        OrderStatus orderStatus = OrderStatus.valueOfStatus(orderUpdateInfo.getStatus());
        if (Objects.isNull(orderStatus)) {
            orderStatus = OrderStatus.valueOfName(orderUpdateInfo.getStatus());
        }
        OrderDetails order = null;
        switch (orderStatus) {
        case PROCESSING:
            order = saveOrderData(orderUpdateInfo, orderStatus);
            offerRuleDetailsService.adjustUserPotential(order, order.getTotalAmount(), 1);
            break;
        case DELIVERED:
            order = getOrderDetailsByOrderId(orderUpdateInfo.getOrderNo());
            if (order.getOrderStatus().equals(OrderStatus.PROCESSING)) {
                if ((orderUpdateInfo.getOrderData().getType().toLowerCase().equals(Constants.COD)
                        || orderUpdateInfo.getOrderData().getType().toLowerCase().equals(Constants.CREDIT))) {
                    if (orderUpdateInfo.getSourceService().equals(ServiceName.SHIPPING)) {
                        order.setTotalAmount(orderUpdateInfo.getShipmentData().getInvoiceAmount());
                        orderDetailsRepository.save(order);
                        updateDeliveryDates(orderUpdateInfo.getOrderNo(), orderStatus);
                    }
                } else {
                    updateDeliveryDates(orderUpdateInfo.getOrderNo(), orderStatus);
                }
            } else {
                log.warn("Not processing {} event for order {} as it is presently in status {}", orderUpdateInfo.getStatus(),
                        orderUpdateInfo.getOrderNo(), order.getOrderStatus());
            }
            break;
        case CANCELLED:
            order = getOrderDetailsByOrderId(orderUpdateInfo.getOrderNo());
            if (order.getOrderStatus().equals(OrderStatus.PROCESSING)) {
                updateOrderStatus(order, orderStatus);
                offerRuleDetailsService.adjustUserPotential(order, orderUpdateInfo.getOrderData().getTotalAmount(), -1);
                offerRuleDetailsService.checkAndProcessOfferRewards(order);
            } else {
                log.warn("Not processing {} event for order {} as it is presently in status {}", orderUpdateInfo.getStatus(),
                        orderUpdateInfo.getOrderNo(), order.getOrderStatus());
            }
            break;
        case RTO_DELIVERED:
            break;
        case SHIPPED_AND_RETURNED:
            order = getOrderDetailsByOrderId(orderUpdateInfo.getOrderNo());
            if (order.getOrderStatus().equals(OrderStatus.PROCESSING)) {
                updateOrderStatus(order, orderStatus);
                offerRuleDetailsService.adjustUserPotential(order, orderUpdateInfo.getShipmentData().getCollectibleAmount().floatValue(), -1);
                offerRuleDetailsService.checkAndProcessOfferRewards(order);
            } else {
                log.warn("Not processing {} event for order {} as it is presently in status {}", orderUpdateInfo.getStatus(),
                        orderUpdateInfo.getOrderNo(), order.getOrderStatus());
            }
            break;
//            Only listening to the REFUND_INITIATED event for the item level cancellation and returns
//        case REFUND_INITIATED:
//        case REPLACEMENT_ORDER_SENT:
//            order = getOrderDetailsByOrderId(orderUpdateInfo.getOrderNo());
//            if (order.getOrderStatus().equals(OrderStatus.RETURN_IN_PROGRESS) || order.getOrderStatus().equals(OrderStatus.DELIVERED)) {
//                updateOrderStatus(order, orderStatus);
//                offerRuleDetailsService.checkAndProcessOfferRewards(order);
//            } else {
//                log.warn("Not processing {} event for order {} as it is presently in status {}", orderUpdateInfo.getStatus(),
//                        orderUpdateInfo.getOrderNo(), order.getOrderStatus());
//            }
//            break;
//        case RETURN_IN_PROGRESS:
        case REFUND_INITIATED:
            order = getOrderDetailsByOrderId(orderUpdateInfo.getOrderNo());
            if (ServiceName.REFUND.equals(orderUpdateInfo.getSourceService())) {
                updateRefundAmount(orderUpdateInfo);
                offerRuleDetailsService.adjustUserPotential(order, orderUpdateInfo.getRefundData().getTotalRefundAmount().floatValue(), -1);
                if (OrderStatus.DELIVERED.equals(order.getOrderStatus())) {
                    offerRuleDetailsService.checkAndProcessOfferRewards(order);
                }
            } else {
                log.warn(
                        "Not processing {} event for order {} as source service is not REFUND",
                        orderUpdateInfo.getStatus(), orderUpdateInfo.getOrderNo());
            }
            break;
        default:
            throw new LoyaltyException(ErrorCodes.INVALID_ORDER_STATUS);
        }
    }

    private OrderDetails saveOrderData(OrderUpdatesInfo orderUpdateInfo, OrderStatus orderStatus) {
        Optional<OrderDetails> orderDetailOptional = orderDetailsReadRepository.findByOrderId(orderUpdateInfo.getOrderNo());
        if (orderDetailOptional.isPresent()) {
            throw new LoyaltyException(ErrorCodes.ORDER_ALREADY_PRESENT);
        } 
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setOrderId(orderUpdateInfo.getOrderNo());
        orderDetails.setOrderStatus(orderStatus);
        OrderData orderData = orderUpdateInfo.getOrderData();
        orderDetails.setUserId(orderData.getUserId());
        orderDetails.setTotalAmount(orderData.getTotalAmount());
        orderDetails.setLifecycleCompleteDate(
                DateUtil.getDateAfterDays(SystemPropertyUtil.getOrderLifecycleCompleteDays()));
        return orderDetailsRepository.save(orderDetails);
    }

    private OrderDetails getOrderDetailsByOrderId(String orderNo) {
        Optional<OrderDetails> orderDetailOptional = orderDetailsReadRepository.findByOrderId(orderNo);
        if (!orderDetailOptional.isPresent()) {
            throw new LoyaltyException(ErrorCodes.ORDER_NOT_FOUND);
        }
        return orderDetailOptional.get();
    }


    private void updateOrderStatus(OrderDetails orderDetails, OrderStatus orderStatus) {
        orderDetails.setOrderStatus(orderStatus);
        orderDetailsRepository.save(orderDetails);
    }

    private void updateDeliveryDates(String orderNo, OrderStatus orderStatus) {
        OrderDetails orderDetails = getOrderDetailsByOrderId(orderNo);
        orderDetails.setDeliveryDate(DateUtil.getDateAfterDays(0));
        int returnPeriodInDays = SystemPropertyUtil.getReturnPeriodInDays() + 1;
        orderDetails.setReturnPeriodOverDate(DateUtil.getDateAfterDays(returnPeriodInDays));
        updateOrderStatus(orderDetails, orderStatus);
    }

    private void updateRefundAmount(OrderUpdatesInfo orderUpdateInfo) {
        OrderDetails orderDetails = getOrderDetailsByOrderId(orderUpdateInfo.getOrderNo());
        orderDetails.setTotalAmount(orderDetails.getTotalAmount().floatValue()
                - (orderUpdateInfo.getRefundData().getPrimaryRefundAmount().floatValue()
                        + orderUpdateInfo.getRefundData().getServiceCharge()));
    }

    @Override
    @Async
    public void calculateRewardPoint() {
        log.info("enter calculateRewardPoint");
        Date startDate = DateUtil.getCronStartDate();
        Date endDate = DateUtil.getCronEndDate();
        log.info("finding orders with status delivered ");
        List<OrderDetails> completedOrders = orderDetailsReadRepository.findByOrderStatusAndReturnPeriodOverDateBetween(OrderStatus.DELIVERED, startDate, endDate);
        for (OrderDetails order : completedOrders) {
            offerRuleDetailsService.updateCurrentProgressForOffers(order);
        }
        log.info("exit calculateRewardPoint");
    }

    @Override
    @Async
    public void completeOrderLifecycle() {
        Date startDate = DateUtil.getCronStartDate();
        Date endDate = DateUtil.getCronEndDate();
        List<OrderDetails> completedOrders = orderDetailsReadRepository.findByOrderStatusAndLifecycleCompleteDateBetween(OrderStatus.PROCESSING, startDate, endDate);
        log.info("found {} orders to be marked as completed", completedOrders.size());
        for (OrderDetails order : completedOrders) {
            try {
                OrderStatus status = getOrderStatus(order);
                if (Objects.nonNull(status) && OrderStatus.DELIVERED.equals(status)) {
                    log.info("Order {} is marked delivered at OMS. Updating progress for customer id {} including this order", order.getOrderId(), order.getUserId());
                    offerRuleDetailsService.updateCurrentProgressForOffers(order);
                    log.info("Updated progress for customer id {} including order {}", order.getUserId(), order.getOrderId());
                } else {
                    log.info(
                            "Order {} is still not marked delivered at OMS. Executing settle lifecycle complte and excluding this order",
                            order.getOrderId(), order.getUserId());
                    offerRuleDetailsService.settleLifecycleCompleteOrder(order);
                    log.info("Order lifecycle complete logic executed for order {}", order.getOrderId());
                }
                updateOrderStatus(order, OrderStatus.COMPLETED);
                log.info("marked order {} as {}", order.getOrderId(), OrderStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Unable to process lifecycle complete event for order {}", order.getOrderId());
                order.setLifecycleCompleteDate(DateUtil.getDateAfterDays(1));
                orderDetailsRepository.save(order);
            }
            
        } 
    }

    
    private OrderStatus getOrderStatus(OrderDetails order) throws JsonMappingException, JsonProcessingException {
        StringBuilder url = new StringBuilder(SystemPropertyUtil.getOmsAggregatorHost());
        url.append(Constants.Oms.GET_ORDER_BY_ID_PATH).append(order.getOrderId());
        JsonNode response = restServiceHelper.getForData(url.toString());
        if (Objects.isNull(response) || response.isEmpty()) {
            log.error("received empty response from OMS for order {}", order.getOrderId());
        }
        String orderState = response.get(Constants.Oms.STATE).asText();
        OrderStatus orderStatus = OrderStatus.valueOfStatus(orderState);
        if (Objects.isNull(orderStatus)) {
            orderStatus = OrderStatus.valueOfName(orderState);
        }
        return orderStatus;
    }

    @Override
    public OrderDetails updateOrderDetails(OrderDetails requestDto) {
        log.info("enter updateOrderDetails for order id {}", requestDto.getOrderId());
        OrderDetails orderDetails = getOrderDetailsByOrderId(requestDto.getOrderId());
        orderDetails.setDeliveryDate(requestDto.getDeliveryDate());
        orderDetails.setLifecycleCompleteDate(requestDto.getLifecycleCompleteDate());
        orderDetails.setOrderStatus(requestDto.getOrderStatus());
        orderDetails.setReturnPeriodOverDate(requestDto.getReturnPeriodOverDate());
        orderDetails.setTotalAmount(requestDto.getTotalAmount());
        orderDetails = orderDetailsRepository.save(orderDetails);
        int factor = !orderDetails.getOrderStatus().equals(OrderStatus.PROCESSING) && !orderDetails.getOrderStatus().equals(OrderStatus.DELIVERED) ? 1 : -1;
        offerRuleDetailsService.adjustUserPotential(orderDetails, orderDetails.getTotalAmount(), factor);
        log.info("successfully updated order details for order id {}", requestDto.getOrderId());
        return orderDetails;
    }

    @Override
    public OrderDetails createOrderDetails(OrderDetails requestDto) {
        Optional<OrderDetails> orderDetailOptional = orderDetailsReadRepository.findByOrderId(requestDto.getOrderId());
        if (orderDetailOptional.isPresent()) {
            throw new LoyaltyException(ErrorCodes.ORDER_ALREADY_PRESENT);
        } 
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setOrderId(requestDto.getOrderId());
        orderDetails.setOrderStatus(OrderStatus.PROCESSING);
        orderDetails.setUserId(requestDto.getUserId());
        orderDetails.setTotalAmount(requestDto.getTotalAmount());
        orderDetails.setLifecycleCompleteDate(
                DateUtil.getDateAfterDays(SystemPropertyUtil.getOrderLifecycleCompleteDays()));
        orderDetails = orderDetailsRepository.save(orderDetails);
        offerRuleDetailsService.adjustUserPotential(orderDetails, orderDetails.getTotalAmount(), 1);
        log.info("successfully created order details for order id {}", requestDto.getOrderId());
        return orderDetails;
    }
}
