package com.nykaa.loyalty.controller;

import com.nykaa.cs.service.ServiceMetaData;
import com.nykaa.loyalty.dto.SystemPropertyDTO;
import com.nykaa.loyalty.entity.SystemProperty;
import com.nykaa.loyalty.service.AdminService;
import com.nykaa.loyalty.service.LoyaltyStartupService;
import com.nykaa.loyalty.service.SystemPropertyService;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.EventLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.nykaa.base.events.enums.EventFieldEnum.ATTRIBUTES;

@Controller("loyaltySystemPropertyController")
@RequestMapping("/loyalty/system/property")
@Slf4j
public class SystemPropertyController {

    @Autowired
    @Qualifier("loyaltySystemPropertyService")
    private SystemPropertyService systemPropertyService;

    @Autowired
    @Qualifier("loyaltyAdminService")
    private AdminService adminService;

    @Autowired
    @Qualifier("loyaltyServiceMetaDataService")
    private ServiceMetaData serviceMetaData;

    @Autowired
    @Qualifier("loyaltyStartupService")
    private LoyaltyStartupService loyaltyStartupService;

    @RequestMapping(value = "/list")
    public String getSystemPropertyList(Model model) {
        try {
            List<SystemProperty> dtoList = systemPropertyService.findAll();
            model.addAttribute(Constants.AdminPanel.DTO_LIST, dtoList);
            model.addAttribute(Constants.AdminPanel.APP_NAME, serviceMetaData.getApplicationName());
        } catch (Exception e) {
            EventLogUtil.customEventExceptionLog(e);
            log.error("Exception occurred while getting system property list, exception: "+ e);
        }
        return "superstore-loyalty";
    }

    @PostMapping("/saveOrUpdate")
    public String updateSystemProperty(SystemPropertyDTO systemPropertyDTO, RedirectAttributes redirectAttributes) {
        boolean isCreateRequest = systemPropertyDTO.getId() == null;
        try {
            MDC.put(ATTRIBUTES.name(), systemPropertyDTO.getName());
            systemPropertyService.saveOrUpdateSystemProperty(systemPropertyDTO);
            adminService.updateRedirectAttributesForSaveOrUpdate(redirectAttributes, isCreateRequest, true);
            log.info("Successfully save/update system property with name: {}", systemPropertyDTO.getName());
        } catch (Exception ex) {
            log.error("Save/Update system property failed due to {}", ex.getMessage());
            adminService.updateRedirectAttributesForSaveOrUpdate(redirectAttributes, isCreateRequest, false);
            EventLogUtil.customEventExceptionLog(ex);
        }
        return "redirect:/loyalty/system/property/list";
    }

    @RequestMapping(value = "/refresh")
    public String loadSystemProperties(RedirectAttributes redirectAttributes) {
        try {
            loyaltyStartupService.loadSystemProperties();
            adminService.updateRedirectAttributesForRefresh(redirectAttributes);
        } catch (Exception e) {
            EventLogUtil.customEventExceptionLog(e);
            log.error("Exception occurred while reloading system property cache, exception: ", e);
        }
        return "redirect:/loyalty/system/property/list";
    }

    @RequestMapping(value = "/module")
    public String module(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                model.addAttribute(Constants.AdminPanel.EMAIL, authentication.getName());
            }
            model.addAttribute(Constants.AdminPanel.APP_NAME, serviceMetaData.getApplicationName());
        } catch (Exception e) {
            EventLogUtil.customEventExceptionLog(e);
            log.error("Exception occurred while getting module for vault, exception: ", e);
        }
        return "module";
    }
}