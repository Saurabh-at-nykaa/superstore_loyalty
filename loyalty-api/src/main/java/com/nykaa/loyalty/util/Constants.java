package com.nykaa.loyalty.util;

public interface Constants {

    String LOYALTY_OFFERS_PAGE_SIZE = "loyalty_offers_page_size";
    String LOYALTY_OFFERS_DEFAULT_PAGE_SIZE = "10";
    String SUPERSTORE_DOMAIN = "NykaaD";
    String LOYALTY = "SS_LOYALTY_";
    String DEFAULT = "default";
    String SUPERSTORE_LOYALTY = "superstore_loyalty";
    String SYSTEM = "system";
    String SUCCESS = "success";
    String STATUS_CODE = "statusCode";
    String OK_STATUS_CODE = "200";
    String DATA = "data";
    String ZERO_TIER_MESSAGE_TEMPLATE = "zero_tier_message_template";
    String NEXT_TIER_MESSAGE_TEMPLATE = "next_tier_message_template";
    String DELTA_SPEND = "{deltaSpend}";
    String NEXT_REWARD = "{nextReward}";
    String MAX_REWARD = "{maxReward}";
    String TIER_REWARD = "{tierReward}";
    String DOUBLE_DECIMAL = ".00 ";
    String YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    String MAX_TIER_MESSAGE_TEMPLATE = "max_tier_message_template";
    String ZERO_TIER_OFFER_TITLE = "zero_tier_offer_title";
    String ZERO_TIER_OFFER_SUBTITLE = "zero_tier_offer_subtitle";
    String NEXT_TIER_OFFER_TITLE = "next_tier_offer_title";
    String NEXT_TIER_OFFER_SUBTITLE = "next_tier_offer_subtitle";
    String START_OF_THE_DAY = " 00:00:00' ";
    String END_OF_THE_DAY = " 23:59:59' ";
    String COD = "cod";
    String CREDIT = "credit";
    String EMAIL_REGEX = "(^[^@]{3}|(?!^)\\G)[^@]";
    String MASK_REPLACEMENT = "$1*";

    interface Symbols {
        String COMMA = ",";
        String UNDERSCORE = "_";
        String SPACE = " ";
        String PERCENT = " %";
    }

    interface EmailParams {
        String LOYALTY_NOTIFICATION_TEMPLATE = "LOYALTY_OFFER_MAPPING_FAILED";
        String FILE_NAME = "loyalty_offer_customer_mapping";
    }

    interface EventSchedulerParam {
        String EVENT_NAME = "EventName";
        String EVENT_DATA = "EventData";
        String LOYALTY_START_EVENT = "SuperstoreLoyaltyCreateOfferEvent";
    }

    interface Queues {
        String SUPERSTORE_NEW_USER_EVENT_QUEUE = "SuperstoreNewUserEventQueue";
        String SUPERSTORE_LOYALTY_CREATE_OFFER = "StartLoyaltyOfferEventQueue";
        String SUPERSTORE_LOYALTY_ORDER_UPDATE_QUEUE = "SuperstoreLoyaltyOrderUpdateQueue";
    }

    interface CacheKeys {
        String ALL = "all";
        String SYSTEM = "system";
        String RECIPIENT_MAIL_IDS = "loyalty_recipient_mail_ids";
        String RETRY_LIMIT = "loyalty_start_offer_retry_limit";
        String LOYALTY_START_ADVANCE_TIME = "loyalty_start_advance_time";
        String DEFAULT_LOYALTY_START_ADVANCE_TIME = "10";
        String DEFAULT_LOYALTY_START_OFFER_RETRY = "10";
        String LOYALTY_MAPPING_FAILED_MESSAGE_TEMPLATE = "loyalty_mapping_failed_message_template";
        String RETURN_PERIOD = "return_period";
        String IS_VAULT_ENABLED = "is_vault_enabled";
        String DEFAULT_RETURN_PERIOD = "5";
        String FALSE = "false";
        String ORDER_COMPLETE_DAYS = "order_complete_days";
        String DEFAULT_ORDER_COMPLETE_DAYS = "30";
        String OMS_AGG_HOST = "oms_agg_host";
        String PAST_EARNING_MONTHS = "past_earning_months";
        String DEFAULT_PAST_EARNING_MONTHS = "2";
        String NEXT_MSG_TEMPLATE_MAP = "next_msg_template_map";
        String NO_ACTIVE_OFFER_MSG = "no_active_offer_msg";
        String OFFER_TITLE_TEMPLATE_MAP = "offer_title_template_map";
    }

    interface AdminPanel {
        String EMAIL = "email";
        String DTO_LIST = "dtoList";
        String APP_NAME = "appName";
        String OFFER_TYPE_ENUM_LIST = "offerTypes";
        String REWARD_POINTS_TYPE_LIST = "rewardTypes";
        String REWARD_POINTS_EXPIRY_UNIT_LIST = "rewardExpiryUnits";
        String CLUSTERS = "clusters";
        String CUSTOMER_SEGMENTS = "customerSegments";
        String CUSTOMER_GROUPS = "customerGroups";
        String PAYMENT_METHODS = "paymentMethods";
        String BRAND_LIST = "brands";
        String OFFER_DETAILS_OBJECT = "offerDetails";
        String QUERY_COUNT = "queryCount";
        String QUERY = "query";
        String ERRORS = "errors";
        String DOMAIN_LIST = "domains";
        String TARGET_TYPE_LIST = "targetTypes";
        String CUSTOMER_TYPE = "customerTypes";
    }

    interface SystemProperty {
        String CUSTOMER_GROUPS = "customer_groups";
        String CLUSTERS = "clusters";
        String CATALOG_S3_BUCKET = "catalog_s3_bucket";
        String CATALOG_S3_BRAND_FILE_KEY = "catalog_s3_brand_file_key";
    }

    interface ValidationErrors {
        String SAME_OFFER_EXISTS = "Offer with same date, domain, customer type and funding split and overlapping duration exists";
        String INVALID_FUNDING_SPLIT = "Brand Share + Nykaa Share must equal 100";
        String INVALID_TARGET_FACTOR = "Variable Target multiplier cannot be less than 1";
        String INVALID_TIER_VALUES = "Tier wise reward & target values are missing or not in asc order";
        String VARIABLE_TARGETS_NOT_ALLOWED = "Variable targets are not allowed for selected scheme type";
        String FIXED_REWARD_GREATER_THAN_MAX_REWARD = "Fixed Reward Point cannot be greater than Max Reward";
        String FRACTIONAL_TARGET_NOT_ALLOWED = "Only integer targets are supported for selected scheme type and target type";
    }

    interface Alias {
        String CUSTOMER_ID = "customer_id";
        String TOTAL_ORDER = "total_number_of_orders";
        String AVG_SPEND = "avg_Spends_in_last_N_Month";
        String UNIQUE_CUTS = "total_unique_cuts";
        String UNIQUE_BRANDS = "total_unique_brands";
    }

    interface Environment {
        String PREPROD = "mumbai-preprod";
    }

    interface Oms {
        String GET_ORDER_BY_ID_PATH = "/omsApis/getOrderById?orderId=";
        String STATE = "state";
    }

    interface Dwh {
        String DWH_SYNC_HOUR = "dwh_sync_hour";
        String DWH_SYNC_DEFAULT_HOUR = "7";
        String TIMEOUT = "dwh_query_timeout";
        String DEFAULT_TIMEOUT = "120";
        String RETRY_QUERY_EXECUTION_TIME = "retry_query_execution_in_min";
        String DEFAULT_RETRY_QUERY_EXECUTION_TIME = "5";
    }

    interface MagentoDBDetails {
        String url = "jdbc:mysql://magento-rds-master.preprod-mumbai-nyk.internal:3306/nykaalive1?autoReconnect=true&useSSL=false";
        String password = "oh1ued3phi0uh8ooPh6";
        String username = "nykaalive";
        String className = "com.mysql.cj.jdbc.Driver";
    }
}
