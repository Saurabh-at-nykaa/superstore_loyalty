package com.nykaa.loyalty.enums;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CustomerType {

    LIFETIME_DORMANT, TRANSACTED, NEW_USER;

    public static List<String> getList() {
        return Stream.of(CustomerType.values()).map(CustomerType::name).collect(Collectors.toList());
    }
}
