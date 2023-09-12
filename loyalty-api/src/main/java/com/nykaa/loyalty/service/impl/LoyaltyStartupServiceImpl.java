package com.nykaa.loyalty.service.impl;

import com.nykaa.base.cache.CacheManager;
import com.nykaa.loyalty.repository.read.SystemPropertyReadRepository;
import com.nykaa.loyalty.cache.SystemPropertyCache;
import com.nykaa.loyalty.entity.SystemProperty;
import com.nykaa.loyalty.service.LoyaltyStartupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("loyaltyStartupService")
@Slf4j
public class LoyaltyStartupServiceImpl implements LoyaltyStartupService {

    @Autowired
    @Qualifier("loyaltySystemPropertyReadRepository")
    private SystemPropertyReadRepository systemPropertyReadRepository;

    @Override
    public void loadAllCache() {
        loadSystemProperties();
    }

    @Override
    public void loadSystemProperties() {
        List<SystemProperty> systemProperties = systemPropertyReadRepository.findAll();
        SystemPropertyCache cache = new SystemPropertyCache();
        for (SystemProperty property : systemProperties) {
            cache.getMap().put(property.getName(), property.getValue());
        }
        CacheManager.getInstance().setCache(cache);
        log.info("Successfully loaded system properties to cache.");
    }
}