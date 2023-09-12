package com.nykaa.loyalty.repository.master;

import com.nykaa.loyalty.entity.OfferRuleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRuleDetailsRepository extends JpaRepository<OfferRuleDetails, Long>{

}
