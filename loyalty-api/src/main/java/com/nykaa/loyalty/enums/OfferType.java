package com.nykaa.loyalty.enums;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum OfferType {

    TARGET_SPEND, NO_OF_ORDERS, UNIQUE_LINE_CUTS, UNIQUE_BRANDS;

    public static List<String> getList() {
        return Stream.of(OfferType.values()).map(OfferType::name).collect(Collectors.toList());
    }
}
