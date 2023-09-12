package com.nykaa.loyalty.service;

import com.nykaa.base.dto.order.OrderUpdatesInfo;
import com.nykaa.loyalty.entity.OrderDetails;

public interface OrderDataService {

    void processOrderDataEvent(OrderUpdatesInfo orderUpdateInfo);

    void calculateRewardPoint();

    void completeOrderLifecycle();

    OrderDetails updateOrderDetails(OrderDetails requestDto);

    OrderDetails createOrderDetails(OrderDetails requestDto);

}
