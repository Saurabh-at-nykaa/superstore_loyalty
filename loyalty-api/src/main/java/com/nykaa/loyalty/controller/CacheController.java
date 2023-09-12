package com.nykaa.loyalty.controller;


import com.nykaa.base.response.ResponseBean;
import com.nykaa.loyalty.service.LoyaltyStartupService;
import com.nykaa.loyalty.util.Constants;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.nykaa.base.events.enums.EventFieldEnum.ATTRIBUTES;

@RestController("loyaltyCacheController")
@RequestMapping("/loyalty")
public class CacheController {

    @Autowired
    private LoyaltyStartupService startupService;

    @GetMapping("/loadCache")
    public ResponseBean<?> loadCache(@RequestParam(value = "name", required = false, defaultValue = "all") String name) {
        MDC.put(ATTRIBUTES.name(), name);
        if (Constants.CacheKeys.ALL.equals(name)) {
            startupService.loadAllCache();
        } else if (Constants.CacheKeys.SYSTEM.equals(name)) {
            startupService.loadSystemProperties();
        }
        return new ResponseBean<>("cache reloaded");
    }
}
