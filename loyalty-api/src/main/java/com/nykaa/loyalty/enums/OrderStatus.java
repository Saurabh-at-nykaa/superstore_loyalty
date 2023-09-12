package com.nykaa.loyalty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderStatus {

    PROCESSING("processing"), CANCELLED("cancelled"), DELIVERED("Delivered"), RTO_DELIVERED("RTO-Delivered"),
    SHIPPED_AND_RETURNED("Shipped & Returned"), RETURN_IN_PROGRESS("In Progress"), REFUND_INITIATED("Refund Initiated"),
    REPLACEMENT_ORDER_SENT("Replacement order sent"), COMPLETED("Completed");

    private String status;

    private static final Map<String, OrderStatus> BY_STATUS = new HashMap<>();

    private static final Map<String, OrderStatus> BY_NAME = new HashMap<>();

    static {
        for (OrderStatus e : values()) {
            BY_STATUS.put(e.status, e);
            BY_NAME.put(e.name(), e);
        }
    }

    public static OrderStatus valueOfStatus(String label) {
        return BY_STATUS.get(label);
    }

    public static OrderStatus valueOfName(String label) {
        return BY_NAME.get(label);
    }
}
