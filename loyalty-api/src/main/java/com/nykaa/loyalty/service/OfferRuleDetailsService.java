package com.nykaa.loyalty.service;

import com.nykaa.loyalty.dto.CustomerOfferMappingDto;
import com.nykaa.loyalty.dto.OfferRuleDetailsDto;
import com.nykaa.loyalty.dto.QueryOfferMappingDto;
import com.nykaa.loyalty.dto.UserLoyaltyDetailsDto;
import com.nykaa.loyalty.dto.UserOfferResponseDto;
import com.nykaa.loyalty.entity.LoyaltyCustomerOfferMapping;
import com.nykaa.loyalty.entity.OrderDetails;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.superstore.loyalty.dto.LoyaltyEventRequestDto;
import com.nykaa.superstore.loyalty.dto.NewUserEventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OfferRuleDetailsService {

    OfferRuleDetailsDto createOfferRule(@Valid OfferRuleDetailsDto offerRuleDetails) throws LoyaltyException;

    OfferRuleDetailsDto updateOfferRule(@Valid OfferRuleDetailsDto offerRuleDetails);

    OfferRuleDetailsDto getOfferRuleById(Long offerRuleId);

    Page<OfferRuleDetailsDto> getOfferRules(Pageable pageable);

    Set<String> getUniqueClusters();

    Map<Long, String> getCustomerGroups();

    Map<Long, String> getBrands();

    void toggleStatus(Long id, boolean status);

    List<UserLoyaltyDetailsDto> getCurrentLoyaltyOffers(String customerId);

    OfferRuleDetailsDto getOfferRuleByName(String offerName);

    void mapNewUserToOffer(NewUserEventDTO newUserEventDTO);

    void startLoyaltyOffer(LoyaltyEventRequestDto requestDto) throws Exception;

    Map<String, Object> validateAndGetQueryCount(OfferRuleDetailsDto offerDetails);

    void updateCurrentProgressForOffers(OrderDetails order);

    void endLoyaltyOffer(LoyaltyEventRequestDto requestDto);

    void checkAndProcessOfferRewards(OrderDetails order);

    void retryFailedTransaction();

    void adjustUserPotential(OrderDetails order, float refundAmount, int factor);

    UserOfferResponseDto getUserLoyaltyOffers(String customerId);

    void settleLifecycleCompleteOrder(OrderDetails orderDetails);

    void mapCustomerToOffer(CustomerOfferMappingDto requestDto);

    List<Map<String, String>> executeQuery(QueryOfferMappingDto requestDto);

    LoyaltyCustomerOfferMapping updateCustomerOfferMapping(LoyaltyCustomerOfferMapping requestDto);

    void mapLoyaltyOffers() throws Exception;
}
