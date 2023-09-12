package com.nykaa.loyalty.repository.read;

import com.nykaa.loyalty.entity.OfferRuleDetails;
import com.nykaa.loyalty.enums.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRuleDetailsReadRepository extends JpaRepository<OfferRuleDetails, Long>{

    Optional<OfferRuleDetails> findByOfferRuleName(String offerRuleName);

    List<OfferRuleDetails> findByCustomerTypeAndIsActiveAndStartDateLessThanAndEndDateGreaterThan(CustomerType customerType,
                                                                                                  boolean isActive,
                                                                                                  Date toDate,
                                                                                                  Date fromDate);

    Optional<OfferRuleDetails> findByIdAndIsActive(long offerId, boolean isActive);

    @Query(value = "Select * from offer_rule_details where domain = :domain AND customer_type = :customerType AND "
            + "offer_type = :offerType AND brand_share = :brandShare AND nykaa_share = :nykaaShare AND " +
            "start_date <= :endDate AND :startDate <= end_date", nativeQuery = true)
    Optional<OfferRuleDetails> findByDomainAndOfferTypeAndStartDateAndEndDateAndBrandShareAndNykaaShareAndCustomerType(
            String domain, String offerType, Date startDate, Date endDate, Double brandShare, Double nykaaShare,
            String customerType);

    List<OfferRuleDetails> findByMappingDoneAndIsActiveAndStartDateLessThanAndEndDateGreaterThan(boolean mappingDone, boolean isActive,
                                                                               Date start, Date end);
}
