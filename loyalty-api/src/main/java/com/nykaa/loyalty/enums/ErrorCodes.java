package com.nykaa.loyalty.enums;

import lombok.Getter;

@Getter
public enum ErrorCodes {

    INVALID_SYSTEM_PROPERTY("LOYALTY4001", "Invalid system property: "),
    ID_MISSING("LOYALTY4002", "Id is missing in request"), 
    OFFER_RULE_NOT_FOUND("LOYALTY4003", "Offer rule not found"),
    OFFER_RULE_NAME_ALREADY_EXISTS("LOYALTY4004", "Offer rule with this name already exists"),
    INVALID_DOMAIN("LOYALTY4006", "Invalid domain"),
    NULL_MESSAGE("LOYALTY4007", "Cannot send null message to SQS queue"),
    NULL_QUEUE("LOYALTY4008", "Cannot send message to null queue"),
    DATE_PARSE_ERROR("LOYALTY5001", "Cannot parse provided dates"),
    QUERY_BUILDING_ERROR("LOYALTY5002", "Exception occured while building loyalty query"),
    REDSHIFT_CONNECTION_ERROR("LOYALTY5003", "Exception while making redshift connection"),
    EVENT_SCHEDULER_ERROR("LOYALTY5004", "Exception while sending event to queue"),
    OFFER_END_DATE_SURPASSED("LOYALTY4007", "Offer end date surpassed"),
    UPDATE_NOT_ALLOWED("LOYALTY4005", "Offer update is not allowed after start date"),
    OFFER_CUSTOMER_MAPPING_ERROR("LOYALTY5005", "Exception in mapping offer to users"), 
    ORDER_NOT_FOUND("LOYALTY5006", "Order Not Found"),
    INVALID_ORDER_STATUS("LOYALTY5007", "Invalid Order Status"),
    ORDER_ALREADY_PRESENT("LOYALTY5008", "Order Details are already present"),
    REDSHIFT_QUERY_TIMEOUT("LOYALTY5009", "Redshift timeout occurred");

    String code;
    String message;

    ErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
