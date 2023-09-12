package com.nykaa.loyalty.service.impl;

import com.nykaa.cs.service.ServiceMetaData;
import com.nykaa.loyalty.service.LoyaltyStartupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("loyaltyServiceMetaDataService")
public class ServiceMetaDataImpl implements ServiceMetaData {

    @Autowired
    @Qualifier("loyaltyStartupService")
    private LoyaltyStartupService loyaltyStartupService;

    @Override
    public String getApplicationName() {
        return "superstore-loyalty";
    }

    @Override
    public void reloadSystemCache() {
        loyaltyStartupService.loadSystemProperties();
    }
}
