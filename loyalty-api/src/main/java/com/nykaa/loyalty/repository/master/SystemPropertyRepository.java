package com.nykaa.loyalty.repository.master;

import com.nykaa.loyalty.entity.SystemProperty;
import org.springframework.stereotype.Repository;

@Repository("loyaltySystemPropertyRepository")
public interface SystemPropertyRepository extends BaseRepository<SystemProperty, Long> {

    SystemProperty findByName(String name);

}
