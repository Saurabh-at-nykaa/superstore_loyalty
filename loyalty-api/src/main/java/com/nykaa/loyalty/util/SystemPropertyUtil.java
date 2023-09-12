package com.nykaa.loyalty.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nykaa.base.cache.CacheManager;
import com.nykaa.loyalty.cache.SystemPropertyCache;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.nykaa.loyalty.util.Constants.SystemProperty.CLUSTERS;
import static com.nykaa.loyalty.util.Constants.SystemProperty.CUSTOMER_GROUPS;

@Slf4j
public class SystemPropertyUtil {
    
    private static HashMap<Long, String> customerGroupMap;

    private static HashSet<String> clusterSet;

    public static String getAll() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.ALL, StringUtils.EMPTY);
    }

    public static Map<Long, String> getCustomerGroups() {
        if(CollectionUtils.sizeIsEmpty(customerGroupMap)) {
            SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
            String customerGroups = systemPropertyCache.getMap().get(CUSTOMER_GROUPS);
            customerGroupMap = toHashMap(customerGroups);
        }
        return customerGroupMap;
    }


    public static Set<String> getUniqueClusters() {
        if(CollectionUtils.isEmpty(clusterSet)) {
            SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
            String clusters = systemPropertyCache.getMap().get(CLUSTERS);
            clusterSet = toHasSet(clusters);
        }
        return clusterSet;
    }
    
    private static HashSet<String> toHasSet(String input) {
        return new Gson().fromJson(input, new TypeToken<HashSet<String>>(){}.getType());
    }

    private static HashMap<Long, String> toHashMap(String input) {
        return new Gson().fromJson(input, new TypeToken<HashMap<Long, String>>(){}.getType());
    }

    public static void refreshSystemProperties() {
        customerGroupMap = null;
        getCustomerGroups();
        clusterSet = null;
        getUniqueClusters();
    }

    public static String getCatalogBucket() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().get(Constants.SystemProperty.CATALOG_S3_BUCKET);
    }

    public static String getBrandFileKey() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().get(Constants.SystemProperty.CATALOG_S3_BRAND_FILE_KEY);
    }

    public static String getRecipientsMailIds() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.RECIPIENT_MAIL_IDS, StringUtils.EMPTY);
    }

    public static int getLoyaltyStartEventDelay() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return Integer.valueOf(systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.LOYALTY_START_ADVANCE_TIME,
                Constants.CacheKeys.DEFAULT_LOYALTY_START_ADVANCE_TIME));
    }

    public static String getLoyaltyMappingFailedContent() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.LOYALTY_MAPPING_FAILED_MESSAGE_TEMPLATE,
                StringUtils.EMPTY);
    }

    public static int getReturnPeriodInDays() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return Integer.valueOf(systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.RETURN_PERIOD,
                Constants.CacheKeys.DEFAULT_RETURN_PERIOD));
    }

    public static boolean isVaultEnabled() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return Boolean.valueOf(
                systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.IS_VAULT_ENABLED, Constants.CacheKeys.FALSE))
                .booleanValue();
    }
    
    public static String getProperty(String propertyName, String defaultValue) {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        if (!systemPropertyCache.getMap().containsKey(propertyName)) {
            if (StringUtils.isBlank(defaultValue)) {
                log.error("System property with property name : {} not found and no default value is provided", propertyName);
                throw new LoyaltyException(ErrorCodes.INVALID_SYSTEM_PROPERTY);
            }
            log.warn("System property with property name : {} not found in cache return default value {}", propertyName, defaultValue);
            return defaultValue;
        }
        return systemPropertyCache.getMap().get(propertyName);
    }

    public static int getOrderLifecycleCompleteDays() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return Integer.valueOf(systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.ORDER_COMPLETE_DAYS,
                Constants.CacheKeys.DEFAULT_ORDER_COMPLETE_DAYS));
    }

    public static String getOmsAggregatorHost() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.OMS_AGG_HOST,
                StringUtils.EMPTY);
    }

    public static int getPastEarningMonths() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return Integer.valueOf(systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.PAST_EARNING_MONTHS,
                Constants.CacheKeys.DEFAULT_PAST_EARNING_MONTHS));
    }

    public static String getMessageTemplate(String key) {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().getOrDefault(key,
                StringUtils.EMPTY);
    }

    public static HashMap<String, String> getNextMessageTemplateMap(String key) {
        try {
            SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
            String templateMapString = systemPropertyCache.getMap()
                    .getOrDefault(Constants.CacheKeys.NEXT_MSG_TEMPLATE_MAP, StringUtils.EMPTY);
            TypeReference<HashMap<String, HashMap<String, String>>> typeRef = new TypeReference<HashMap<String, HashMap<String, String>>>() {};
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, HashMap<String, String>> templateMap = mapper.readValue(templateMapString, typeRef);
            return templateMap.get(key);
        } catch (JsonProcessingException e) {
            log.error("Error finding next message template map");
            return new HashMap<>();
        }
    }

    public static String getNoActiveOfferMessage() {
        SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
        return systemPropertyCache.getMap().getOrDefault(Constants.CacheKeys.NO_ACTIVE_OFFER_MSG,
                StringUtils.EMPTY);
    }

    public static HashMap<String, String> getOfferTitleMap(String key) {
        try {
            SystemPropertyCache systemPropertyCache = CacheManager.getInstance().getCache(SystemPropertyCache.class);
            String templateMapString = systemPropertyCache.getMap()
                    .getOrDefault(Constants.CacheKeys.OFFER_TITLE_TEMPLATE_MAP, StringUtils.EMPTY);
            TypeReference<HashMap<String, HashMap<String, String>>> typeRef = new TypeReference<HashMap<String, HashMap<String, String>>>() {};
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, HashMap<String, String>> templateMap = mapper.readValue(templateMapString, typeRef);
            return templateMap.get(key);
        } catch (JsonProcessingException e) {
            log.error("Error finding offer title template map");
            return new HashMap<>();
        }
    }
}
