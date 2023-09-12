package com.nykaa.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nykaa.loyalty.enums.CustomerType;
import com.nykaa.loyalty.enums.Domain;
import com.nykaa.loyalty.enums.OfferType;
import com.nykaa.loyalty.enums.RewardPointsExpiryUnit;
import com.nykaa.loyalty.enums.RewardPointsType;
import com.nykaa.loyalty.enums.TargetType;
import com.nykaa.loyalty.util.jpa.converter.JpaConverterListString;
import com.nykaa.superstore.loyalty.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "offer_rule_details")
public class OfferRuleDetails extends BaseEntity {

    @Column(name = "offer_rule_name")
    private String offerRuleName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "domain")
    @Enumerated(EnumType.STRING)
    private Domain domain;

    @Column(name = "nykaa_share")
    private Double nykaaShare;

    @Column(name = "brand_share")
    private Double brandShare;

    @Column(name = "offer_type")
    @Enumerated(EnumType.STRING)
    private OfferType offerType;

    @Column(name = "target_type")
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(name = "target_values")
    @Convert(converter = JpaConverterListString.class)
    private List<String> targetValues;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_point_type")
    private RewardPointsType rewardPointType;

    @Column(name = "reward_point_values")
    @Convert(converter = JpaConverterListString.class)
    private List<String> rewardPointValues;

    @Column(name = "max_reward")
    private Double maxReward;

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
    private Date startDate;

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
    private Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_point_expiry_unit")
    private RewardPointsExpiryUnit rewardPointExpiryUnit;

    @Column(name = "reward_point_expiry_value")
    private Integer rewardPointExpiryValue;

    @Column(name = "fixed_reward_point_expiry")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "IST")
    private Date fixedRewardPointExpiry;

    @Column(name = "query")
    private String query;

    @Column(name = "query_count")
    private Long queryCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @Column(name = "offer_title")
    private String offerTitle;

    @Column(name = "offer_subtitle")
    private String offerSubtitle;

    @Column(name = "mapping_done")
    private Boolean mappingDone = Boolean.FALSE;

}
