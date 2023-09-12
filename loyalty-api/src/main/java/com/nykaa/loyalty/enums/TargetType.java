package com.nykaa.loyalty.enums;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TargetType {

    FIXED, VARIABLE;

    public static List<String> getList() {
        return Stream.of(TargetType.values()).map(TargetType::name).collect(Collectors.toList());
    }

}
