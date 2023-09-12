package com.nykaa.loyalty.service;

import com.nykaa.loyalty.dto.SystemPropertyDTO;
import com.nykaa.loyalty.entity.SystemProperty;

import java.util.List;

public interface SystemPropertyService {
    SystemProperty saveOrUpdateSystemProperty(SystemPropertyDTO systemPropertyDTO);

    List<SystemProperty> findAll();
}
