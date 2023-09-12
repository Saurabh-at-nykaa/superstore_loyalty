package com.nykaa.loyalty.enums;

public enum EmailParams {

    LOYALTY_OFFER("LOYALTY_OFFERS");

    String name;

    EmailParams(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

