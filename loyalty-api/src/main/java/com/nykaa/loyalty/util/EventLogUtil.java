package com.nykaa.loyalty.util;

import com.nykaa.base.events.log4j.CustomEventLogger;
import org.slf4j.MDC;

import static com.nykaa.base.events.enums.EventFieldEnum.REQUEST_URL;
import static com.nykaa.base.events.enums.EventFieldEnum.RESPONSE_STATUS;
import static com.nykaa.base.events.enums.EventFieldEnum.TENANT_ID;

public class EventLogUtil {

    /**
     * Handling Custom Exception and logging events for the same
     *
     * @param ex that is thrown
     */
    public static void customEventExceptionLog(Exception ex) {
        MDC.put(RESPONSE_STATUS.name(), String.valueOf(false));
        CustomEventLogger.publishEvent("CustomException", MDC.get(TENANT_ID.name()), null,
                MDC.get("requestId"), null, null, MDC.get(REQUEST_URL.name()), ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}
