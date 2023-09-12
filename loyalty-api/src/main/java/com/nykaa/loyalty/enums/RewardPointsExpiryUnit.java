package com.nykaa.loyalty.enums;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum RewardPointsExpiryUnit {

    DAYS, MONTHS, YEARS;

    public static List<String> getList() {
        return Stream.of(RewardPointsExpiryUnit.values()).map(RewardPointsExpiryUnit::name).collect(Collectors.toList());
    }
}
