package com.nykaa.loyalty.dto;

import com.nykaa.loyalty.enums.CustomerType;
import lombok.Data;

import java.util.List;

@Data
public class UserSegmentQuery {

    private List<Long> customerGroups;
    private List<String> clusters;
    private List<String> brandNames;
    private Double minAvgSpend;
    private Double maxAvgSpend;
    private String avgSpendStartDate;
    private String avgSpendEndDate;
    private String orderPlacedStartDate;
    private String orderPlacedEndDate;
    private String kycVerifiedStartDate;
    private String kycVerifiedEndDate;
    private CustomerType customerType;
    private Integer noOfOrders = 0;
}
