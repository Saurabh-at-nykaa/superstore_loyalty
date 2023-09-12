package com.nykaa.loyalty.enums;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum Domain {
    NYKAA, NYKAA_D, NYKAA_FASHION, NYKAA_MEN;

    public static List<String> getList() {
        return Stream.of(Domain.values()).map(Domain::name).collect(Collectors.toList());
    }
}
