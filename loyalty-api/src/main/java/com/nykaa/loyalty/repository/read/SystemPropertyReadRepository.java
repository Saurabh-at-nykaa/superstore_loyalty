package com.nykaa.loyalty.repository.read;

import com.nykaa.loyalty.entity.SystemProperty;
import org.springframework.stereotype.Repository;

@Repository("loyaltySystemPropertyReadRepository")
public interface SystemPropertyReadRepository extends BaseReadRepository<SystemProperty, Long> {

    SystemProperty findByName(String name);

}
