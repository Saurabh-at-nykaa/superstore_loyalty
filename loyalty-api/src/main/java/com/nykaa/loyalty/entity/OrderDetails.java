package com.nykaa.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nykaa.loyalty.enums.OrderStatus;
import com.nykaa.superstore.loyalty.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_details")
public class OrderDetails extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "total_amount")
    private Float totalAmount;

    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "delivery_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
    private Date deliveryDate;

    @Column(name = "return_period_over_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
    private Date returnPeriodOverDate;

    @Column(name = "lifecycle_complete_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
    private Date lifecycleCompleteDate;

}
