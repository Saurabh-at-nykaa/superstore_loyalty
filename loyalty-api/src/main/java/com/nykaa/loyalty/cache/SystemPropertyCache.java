package com.nykaa.loyalty.cache;

import com.nykaa.base.annotation.Cache;

import java.util.HashMap;
import java.util.Map;

@Cache(name = "loyaltySystemPropertyCache")
public class SystemPropertyCache {
    private final Map<String, String> map = new HashMap<>();

    public Map<String, String> getMap() {
        return map;
    }
}
