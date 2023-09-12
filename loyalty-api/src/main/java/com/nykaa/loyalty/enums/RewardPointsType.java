package com.nykaa.loyalty.enums;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum RewardPointsType {

    FLAT,PERCENT;

    public static List<String> getList() {
        return Stream.of(RewardPointsType.values()).map(RewardPointsType::name).collect(Collectors.toList());
    }
}
