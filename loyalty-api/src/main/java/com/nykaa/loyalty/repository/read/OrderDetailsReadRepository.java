package com.nykaa.loyalty.repository.read;

import com.nykaa.loyalty.entity.OrderDetails;
import com.nykaa.loyalty.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailsReadRepository extends JpaRepository<OrderDetails, Long> {

    Optional<OrderDetails> findByOrderId(String orderNo);

    List<OrderDetails> findByOrderStatusAndReturnPeriodOverDate(OrderStatus status, Date returnPeriodOverDate);

    List<OrderDetails> findByOrderStatusAndReturnPeriodOverDateBetween(OrderStatus status, Date startDate, Date endDate);

    boolean existsByUserIdAndOrderStatusAndCreatedBetween(String customerId, OrderStatus status, Date startDate,
            Date endDate);

    List<OrderDetails> findByOrderStatusAndLifecycleCompleteDate(OrderStatus status, Date currentDate);

    List<OrderDetails> findByOrderStatusAndLifecycleCompleteDateBetween(OrderStatus status, Date startDate,
            Date endDate);

    boolean existsByUserIdAndOrderStatusAndCreatedBetweenAndOrderIdNot(String customerId, OrderStatus status,
            Date startDate, Date endDate, String orderId);
}
