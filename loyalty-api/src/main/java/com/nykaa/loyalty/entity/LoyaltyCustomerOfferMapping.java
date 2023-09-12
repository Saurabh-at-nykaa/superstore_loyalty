package com.nykaa.loyalty.entity;


import com.nykaa.loyalty.enums.RewardPointsCreditStatus;
import com.nykaa.superstore.loyalty.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "loyalty_customer_offer_mapping")
public class LoyaltyCustomerOfferMapping extends BaseEntity {

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "offer_type_user_result")
    private String offerTypeUserResult = "0";

    @Column(name = "current_progress")
    private String currentProgress = "0";

    @Column(name = "potential_progress")
    private Float potentialProgress = 0.0f;

    @Column(name = "potential_earning")
    private Float potentialEarning = 0.0f;

    @Column(name = "current_tier")
    private Integer currentTier = 0;

    @Column(name = "credited_rp")
    private Float creditedRewardPoints = 0.0f;

    @Column(name = "credit_status")
    @Enumerated(EnumType.STRING)
    private RewardPointsCreditStatus creditStatus;

}
