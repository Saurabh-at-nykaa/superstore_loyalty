package com.nykaa.loyalty.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nykaa.loyalty.enums.Domain;
import com.nykaa.loyalty.enums.OfferType;
import com.nykaa.loyalty.enums.RewardPointsExpiryUnit;
import com.nykaa.loyalty.enums.RewardPointsType;
import com.nykaa.loyalty.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OfferRuleDetailsDto {

    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date created;

    @NotBlank(message = "Offer Rule Name cannot be blank")
    private String offerRuleName;

    private Boolean isActive = true;

    @NotNull(message = "Domain cannot be null")
    private Domain domain;

    @NotNull(message = "Nykaa Share cannot be null")
    private Double nykaaShare;

    @NotNull(message = "Brand Share cannot be null")
    private Double brandShare;

    @NotNull(message = "Scheme Type cannot be null")
    private OfferType offerType;

    @NotNull(message = "Target Type cannot be null")
    private TargetType targetType;

    @NotEmpty(message = "Target Values cannot be null")
    private List<String> targetValues;

    @NotNull(message = "Reward Point Type cannot be null")
    private RewardPointsType rewardPointType;

    @NotEmpty(message = "Reward Point Values cannot be null")
    private List<String> rewardPointValues;

    private Double maxReward;

    @NotNull(message = "Offer Start Date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @NotNull(message = "Offer End Date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @NotNull(message = "Reward Points Expiry Unit cannot be null")
    private RewardPointsExpiryUnit rewardPointExpiryUnit;

    @NotNull(message = "Reward Point Expiry Value cannot be null")
    private Integer rewardPointExpiryValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date fixedRewardPointExpiry;

    private Boolean newUserRule = false;

    private UserSegmentQuery userSegmentQuery;

    private String query;

    private Long queryCount;

    private String offerTitle;

    private String offerSubtitle;
}
