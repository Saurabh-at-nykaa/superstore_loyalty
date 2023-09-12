package com.nykaa.loyalty.repository.master;

import com.nykaa.loyalty.entity.LoyaltyCustomerOfferMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyCustomerOfferMappingRepository extends JpaRepository<LoyaltyCustomerOfferMapping, Long> {
    
}
