package com.nykaa.loyalty.context;

import com.nykaa.loyalty.service.LoyaltyStartupService;
import com.nykaa.loyalty.service.impl.LoyaltyStartupServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Slf4j
public class WebContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Initializing Superstore Loyalty service context ...");
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
        initiliazeLoyaltyStartupService(context);
    }

    private void initiliazeLoyaltyStartupService(WebApplicationContext context) {
        LoyaltyStartupService loyaltyStartupService = context.getBean(LoyaltyStartupServiceImpl.class);
        try {
            loyaltyStartupService.loadAllCache();
            log.info("Successfully initialized Loyalty service context");
        } catch (Exception e) {
            log.error("Error while initializing Loyalty service context", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }
}
