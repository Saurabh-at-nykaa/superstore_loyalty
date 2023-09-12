package com.nykaa.loyalty.controller;

import com.nykaa.base.response.ResponseBean;
import com.nykaa.cs.service.ServiceMetaData;
import com.nykaa.loyalty.dto.OfferRuleDetailsDto;
import com.nykaa.loyalty.dto.UserOfferResponseDto;
import com.nykaa.loyalty.enums.CustomerType;
import com.nykaa.loyalty.enums.Domain;
import com.nykaa.loyalty.enums.HttpStatus;
import com.nykaa.loyalty.enums.OfferType;
import com.nykaa.loyalty.enums.RewardPointsExpiryUnit;
import com.nykaa.loyalty.enums.RewardPointsType;
import com.nykaa.loyalty.enums.TargetType;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.jms.listener.OrderEventListener;
import com.nykaa.loyalty.service.AdminService;
import com.nykaa.loyalty.service.OfferRuleDetailsService;
import com.nykaa.loyalty.service.PreprodDataSyncService;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.EventLogUtil;
import com.nykaa.loyalty.util.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Map;

@Controller
@RequestMapping("/loyalty/offer-rules")
@Slf4j
public class OfferRulesController {

    @Autowired
    private OfferRuleDetailsService offerRuleDetailsService;

    @Autowired
    @Qualifier("loyaltyServiceMetaDataService")
    private ServiceMetaData serviceMetaData;

    @Autowired
    @Qualifier("loyaltyAdminService")
    private AdminService adminService;

    @Autowired
    private PreprodDataSyncService preprodDataSyncService;
    
    @PostMapping("/create")
    @ResponseBody
    ResponseBean<?> createOfferRule(@Valid @RequestBody OfferRuleDetailsDto offerRuleDetails) {
        log.info("request received to create offer rule with data: {}");
        try {
            OfferRuleDetailsDto offerRule = offerRuleDetailsService.createOfferRule(offerRuleDetails);
            return new ResponseBean<>(offerRule);
        } catch (Exception e) {
            log.error("Exception happened in createOfferRule API " + e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/update")
    @ResponseBody
    ResponseBean<?> updateOfferRule(@Valid @RequestBody OfferRuleDetailsDto offerRuleDetails) {
        log.info("request received to update offer rule with data: {}");
        try {
            OfferRuleDetailsDto offerRule = offerRuleDetailsService.updateOfferRule(offerRuleDetails);
            return new ResponseBean<>(offerRule);
        } catch (Exception e) {
            log.error("Exception happened in updateOfferRule API " + e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    ResponseBean<?> getOfferRuleById(@PathVariable("id") Long offerRuleId) {
        log.info("request received to fetch offer rule with id: {}", offerRuleId);
        try {
            OfferRuleDetailsDto offerRule = offerRuleDetailsService.getOfferRuleById(offerRuleId);
            return new ResponseBean<>(offerRule);
        } catch (Exception e) {
            log.error("Exception happened in getOfferRuleById API " + e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @GetMapping("/get-page")
    @ResponseBody
    ResponseBean<Page<OfferRuleDetailsDto>> getOfferRules(Pageable pageable) {
        log.info("request received to fetch offer rules with pageable: {}", pageable.toString());
        try {
            Page<OfferRuleDetailsDto> offerRules = offerRuleDetailsService.getOfferRules(pageable);
            return new ResponseBean<>(offerRules);
        } catch (Exception e) {
            log.error("Exception happened in getOfferRules API " + e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, e.getMessage());
        }
    }

    @PostMapping("/toggle-status/{id}/{status}")
    @ResponseBody
    ResponseBean<?> toggleOfferRuleStatus(@PathVariable Long id, @PathVariable boolean status) {
        log.info("Request to update active status for offer rule with id: {} to status: {}", id, status);
        try {
            offerRuleDetailsService.toggleStatus(id, status);
            return new ResponseBean<>();
        } catch (Exception e) {
            log.error("Exception happened in toggle-status API " + e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, HttpStatus.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    // Config Apis
    @GetMapping("/list")
    public String getOfferRulesList(@RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
                                    @RequestParam(value = "sortField", defaultValue = "offerRuleName", required = false) String sortField,
                                    @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
                                    Model model) {
        try {
            Sort sort = sortDir.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
            int pageSize = Integer.parseInt(SystemPropertyUtil.getProperty(Constants.LOYALTY_OFFERS_PAGE_SIZE,
                    Constants.LOYALTY_OFFERS_DEFAULT_PAGE_SIZE));
            Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
            Page<OfferRuleDetailsDto> page = offerRuleDetailsService.getOfferRules(pageable);
            model.addAttribute(Constants.AdminPanel.DTO_LIST, page.getContent());
            model.addAttribute("flag", Boolean.TRUE);
            model.addAttribute("currentPage", pageNo);
            model.addAttribute("totalPages", page.getTotalPages());
            model.addAttribute("totalItems", page.getTotalElements());
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
            model.addAttribute(Constants.AdminPanel.APP_NAME, serviceMetaData.getApplicationName());
            model.addAttribute(Constants.AdminPanel.OFFER_TYPE_ENUM_LIST, OfferType.getList());
            model.addAttribute(Constants.AdminPanel.REWARD_POINTS_EXPIRY_UNIT_LIST, RewardPointsExpiryUnit.getList());
            model.addAttribute(Constants.AdminPanel.REWARD_POINTS_TYPE_LIST, RewardPointsType.getList());
            model.addAttribute(Constants.AdminPanel.CUSTOMER_GROUPS, offerRuleDetailsService.getCustomerGroups());
            model.addAttribute(Constants.AdminPanel.DOMAIN_LIST, Domain.getList());
            model.addAttribute(Constants.AdminPanel.TARGET_TYPE_LIST, TargetType.getList());
            model.addAttribute(Constants.AdminPanel.CUSTOMER_TYPE, CustomerType.getList());
//            model.addAttribute(Constants.AdminPanel.BRAND_LIST, offerRuleDetailsService.getBrands());
            model.addAttribute(Constants.AdminPanel.CLUSTERS, offerRuleDetailsService.getUniqueClusters());
            model.addAttribute(Constants.AdminPanel.OFFER_DETAILS_OBJECT, new OfferRuleDetailsDto());
        } catch (Exception e) {
            EventLogUtil.customEventExceptionLog(e);
            log.error("Exception occurred while getting offer lists {}", e.getMessage());
        }
        return "loyalty_offers_list";
    }

    @PostMapping("/createOrUpdate")
    public String createOrUpdateLoyaltyOffers(@ModelAttribute("offerDetails") OfferRuleDetailsDto offerRuleDetailsDto,
            RedirectAttributes redirectAttributes) {
        boolean isCreateRequest = offerRuleDetailsDto.getId() == null;
        log.info("Request to create/update offer - {}", offerRuleDetailsDto);
        try {
            if (isCreateRequest) {
                offerRuleDetailsService.createOfferRule(offerRuleDetailsDto);
            } else {
                offerRuleDetailsService.updateOfferRule(offerRuleDetailsDto);
            }
            adminService.updateRedirectAttributesForSaveOrUpdate(redirectAttributes, isCreateRequest, true);
            log.info("Offer details are created/updated successfully");
        } catch (LoyaltyException e) {
            adminService.updateRedirectAttributesForSaveOrUpdate(redirectAttributes, isCreateRequest, false);
            log.error("Exception while creating/updating offer, {}", e.getMessage());
            throw new LoyaltyException(e.getErrorCode(), e.getMessage());
        }
        return "redirect:/loyalty/offer-rules/list";
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

    @PostMapping("/validate-and-get-count")
    @ResponseBody
    ResponseBean<?> validateOfferRuleAndGetCustomerCount(@ModelAttribute("offerDetails") @Valid OfferRuleDetailsDto offerDetails) {
        log.info("Request to validate and get customer count for new offer rule with details {}",
                offerDetails.toString());
        try {
            Map<String, Object> responseAttributes = offerRuleDetailsService.validateAndGetQueryCount(offerDetails);
            return new ResponseBean<>(responseAttributes);
        } catch (Exception e) {
            log.error("Exception happened in validate-and-get-count API " + e.getMessage());
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, HttpStatus.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    @Autowired
    private OrderEventListener eventListener;
    @GetMapping("/test-order")
    ResponseBean<?> testOrder(@RequestBody Map<String, String> request) {
        try {
            eventListener.orderUpdateReceived(request.get("order"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ResponseBean<>();
    }

    @PostMapping("/preprod-data-sync/{minutes}")
    @ResponseBody
    public ResponseBean<?> syncPreprodData(@PathVariable int minutes) throws Exception {
        preprodDataSyncService.syncData(minutes);
        return new ResponseBean<>();
    }

    @GetMapping(value = "/get-user-loyalty/{customerId}")
    @ResponseBody
    public ResponseBean<?> getUserLoyaltyOffers(@PathVariable String customerId) throws Exception {
        log.info("Request to get current offers for customer : {}", customerId);
        try {
            UserOfferResponseDto userLoyalty = offerRuleDetailsService.getUserLoyaltyOffers(customerId);
            return new ResponseBean<>(true, HttpStatus.OK.getCode(), "success", userLoyalty);
        } catch (Exception e) {
            log.error("Exception while fetching offers for customer : {}", customerId);
            EventLogUtil.customEventExceptionLog(e);
            return new ResponseBean<>(false, HttpStatus.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }
}
