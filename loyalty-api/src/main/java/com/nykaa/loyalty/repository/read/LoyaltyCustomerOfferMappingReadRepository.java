package com.nykaa.loyalty.repository.read;

import com.nykaa.loyalty.entity.LoyaltyCustomerOfferMapping;
import com.nykaa.loyalty.enums.RewardPointsCreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyCustomerOfferMappingReadRepository extends JpaRepository<LoyaltyCustomerOfferMapping, Long> {
    
    @Query(value = "Select * from loyalty_customer_offer_mapping where customer_id = :customerId AND " +
            "(start_date <= :endDate AND :startDate <= end_date AND end_date >= CURDATE())", nativeQuery = true)
    List<LoyaltyCustomerOfferMapping> findByCustomerIdAndOfferDurationInRange(String customerId, Date startDate,
                                                                              Date endDate);

    @Query(value = "Select * from loyalty_customer_offer_mapping where customer_id = :customerId AND "
            + "(start_date <= :currentDate AND end_date >= :currentDate)", nativeQuery = true)
    List<LoyaltyCustomerOfferMapping> findByCustomerIdAndRunning(String customerId, Date currentDate);

    List<LoyaltyCustomerOfferMapping> findByOfferId(Long id);

    List<LoyaltyCustomerOfferMapping> findByCreditStatus(RewardPointsCreditStatus failed);

    @Query(value = "Select * from loyalty_customer_offer_mapping where customer_id = :customerId AND "
            + "(start_date <= :currentDate AND end_date >= :currentDate) AND credit_status != 'CREDITED'", nativeQuery = true)
    List<LoyaltyCustomerOfferMapping> findByCustomerIdAndRunningAndStatusNotCredited(String customerId, Date currentDate);

    Optional<LoyaltyCustomerOfferMapping> findByCustomerIdAndOfferId(String customerId, Long offerId);
}
