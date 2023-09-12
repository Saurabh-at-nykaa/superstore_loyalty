package com.nykaa.loyalty.repository.master;

import com.nykaa.loyalty.entity.PincodeClusterMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PincodeClusterMappingRepository extends BaseRepository<PincodeClusterMapping, Long> {

    PincodeClusterMapping findByPincode(String pincode);

    PincodeClusterMapping findByPincodeAndIsActive(String pincode, Boolean isActive);

    List<PincodeClusterMapping> findByDeleted(boolean deleted);

    Page<PincodeClusterMapping> findByUpdatedGreaterThanEqual(Date updated, Pageable pageable);

}
