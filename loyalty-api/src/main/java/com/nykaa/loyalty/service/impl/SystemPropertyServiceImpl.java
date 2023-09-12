package com.nykaa.loyalty.service.impl;

import com.nykaa.loyalty.dto.SystemPropertyDTO;
import com.nykaa.loyalty.entity.SystemProperty;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.repository.master.SystemPropertyRepository;
import com.nykaa.loyalty.repository.read.SystemPropertyReadRepository;
import com.nykaa.loyalty.service.SystemPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("loyaltySystemPropertyService")
public class SystemPropertyServiceImpl implements SystemPropertyService {

    @Autowired
    @Qualifier("loyaltySystemPropertyRepository")
    private SystemPropertyRepository systemPropertyRepository;

    @Autowired
    @Qualifier("loyaltySystemPropertyReadRepository")
    private SystemPropertyReadRepository systemPropertyReadRepository;

    @Override
    public SystemProperty saveOrUpdateSystemProperty(SystemPropertyDTO systemPropertyDTO) {
        SystemProperty systemProperty;
        if (systemPropertyDTO.getId() == null) {
            systemProperty = new SystemProperty();
        } else {
            Optional<SystemProperty> systemPropertyOptional = systemPropertyReadRepository.findById(systemPropertyDTO.getId());
            systemProperty = systemPropertyOptional.orElse(null);
            if (systemProperty == null) {
                throw new LoyaltyException(ErrorCodes.INVALID_SYSTEM_PROPERTY,
                        ErrorCodes.INVALID_SYSTEM_PROPERTY.getMessage() + systemPropertyDTO.getName());
            }
        }

        systemProperty.setId(systemPropertyDTO.getId());
        systemProperty.setName(systemPropertyDTO.getName());
        systemProperty.setValue(systemPropertyDTO.getValue());

        return systemPropertyRepository.save(systemProperty);
    }

    @Override
    public List<SystemProperty> findAll() {
        return systemPropertyReadRepository.findAll();
    }
}