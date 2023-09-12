package com.nykaa.loyalty.controller;

import com.nykaa.loyalty.service.OfferRuleDetailsService;
import com.nykaa.loyalty.service.OrderDataService;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.SystemPropertyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;

@RestController
@RequestMapping("/loyalty/cron")
@RequiredArgsConstructor
@Slf4j
public class LoyaltyCronController {

    private final OrderDataService orderDataService;

    private final OfferRuleDetailsService offerRuleDetailsService;

    @PostMapping("/calcaulate-reward-points")
    public String calculateRewardPoint() {
        log.info("start calculateRewardPoint cron");
        orderDataService.calculateRewardPoint();
        log.info("end calculateRewardPoint cron");
        return "calculate reward point cron ran successfully";
    }

    @PostMapping("/retry-failed-txn")
    public String retryFailedTransaction() {
        log.info("start retryFailedTransaction cron");
        offerRuleDetailsService.retryFailedTransaction();
        log.info("end retryFailedTransaction cron");
        return "retry failed transaction cron ran successfully";
    }

    @PostMapping("/order-lifecycle-complete")
    public String orderLifecycleComplete() {
        log.info("start orderLifecycleComplete cron");
        orderDataService.completeOrderLifecycle();
        log.info("end orderLifecycleComplete cron");
        return "order lifecycle complete cron ran successfully";
    }

    @PostMapping("/start-loyalty-offer")
    public String startLoyaltyOfferSchedular() throws Exception {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int dwhSyncHour = Integer.parseInt(SystemPropertyUtil
                .getProperty(Constants.Dwh.DWH_SYNC_HOUR, Constants.Dwh.DWH_SYNC_DEFAULT_HOUR));
        if (hourOfDay + 1 < dwhSyncHour) {
            log.info("start loyalty offer cron starts after : {} AM", dwhSyncHour);
            return "start loyalty offer cron starts after dwhSyncHour : "+dwhSyncHour;
        }
        log.info("Start Loyalty schedular  started ");
        this.offerRuleDetailsService.mapLoyaltyOffers();
        log.info("Start Loyalty schedular  completed ");
        return "Start Loyalty schedular completed";
    }
}
