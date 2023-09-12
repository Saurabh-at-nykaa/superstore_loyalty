package com.nykaa.superstore.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class NewUserEventDTO {

    private String customerId;
    private Long groupId;
    @JsonProperty("kyc_approve_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date kycApproveDate;
    @JsonProperty(value = "address_data")
    private AddressDataDto addressData;
    private String domain;
}
