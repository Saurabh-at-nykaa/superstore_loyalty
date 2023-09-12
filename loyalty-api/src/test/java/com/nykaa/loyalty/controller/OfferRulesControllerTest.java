package com.nykaa.loyalty.controller;

import com.nykaa.loyalty.AbstractTest;
import com.nykaa.loyalty.dto.Test;
import com.nykaa.loyalty.entity.LoyaltyCustomerOfferMapping;
import com.nykaa.loyalty.entity.OfferRuleDetails;
import com.nykaa.loyalty.repository.master.OfferRuleDetailsRepository;
import com.nykaa.loyalty.repository.read.LoyaltyCustomerOfferMappingReadRepository;
import com.nykaa.loyalty.repository.read.OfferRuleDetailsReadRepository;
import com.nykaa.loyalty.utils.CommonUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.nykaa.loyalty.utils.CommonUtils.doGetTest;
import static com.nykaa.loyalty.utils.CommonUtils.doTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

public class OfferRulesControllerTest extends AbstractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private OfferRuleDetailsReadRepository offerRuleDetailsReadRepository;

    @MockBean
    private OfferRuleDetailsRepository offerRuleDetailsRepository;

    @MockBean
    private LoyaltyCustomerOfferMappingReadRepository loyaltyCustomerOfferMappingReadRepository;

    private static Test[] createOfferRuleTests() throws IOException {
        return CommonUtils.loadTests("classpath:OfferRulesController_createOfferRule.json");
    }

    private static Test[] updateOfferRuleTests() throws IOException {
        return CommonUtils.loadTests("classpath:OfferRulesController_updateOfferRule.json");
    }

    private static Test[] getOfferRuleByIdTests() throws IOException {
        return CommonUtils.loadTests("classpath:OfferRulesController_getOfferRuleById.json");
    }


    private static Test[] getOfferRulesTests() throws IOException {
        return CommonUtils.loadTests("classpath:OfferRulesController_getOfferRules.json");
    }

    private static Test[] toggleOfferRuleStatus() throws IOException {
        return CommonUtils.loadTests("classpath:OfferRulesController_toggleOfferRuleStatus.json");
    }

    private static Test[] getOfferDetailsByCustomer() throws IOException {
        return CommonUtils.loadTests("classpath:OfferRulesController_getOfferDetailsByCustomer.json");
    }

    @ParameterizedTest
    @MethodSource("createOfferRuleTests")
    public void testCreateOfferRuleTests(Test test) throws Exception {
        doReturn(
                CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("savedOfferRule"), OfferRuleDetails.class))
                .when(offerRuleDetailsRepository).save(any(OfferRuleDetails.class));
        doReturn(Optional.ofNullable(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("existingOfferRule"),
                OfferRuleDetails.class))).when(offerRuleDetailsReadRepository).findByOfferRuleName(Mockito.anyString());
        doTest(test, mockMvc, "/loyalty/offer-rules/create");
    }

    @ParameterizedTest
    @MethodSource("updateOfferRuleTests")
    public void testUpdateOfferRuleTests(Test test) throws Exception {
        doReturn(Optional.ofNullable(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("existingOfferRule"), OfferRuleDetails.class)))
        .when(offerRuleDetailsReadRepository).findById(anyLong());
        doReturn(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("savedOfferRule"), OfferRuleDetails.class))
                .when(offerRuleDetailsReadRepository).save(any(OfferRuleDetails.class));
        doTest(test, mockMvc, "/loyalty/offer-rules/update");
    }

    @ParameterizedTest
    @MethodSource("getOfferRuleByIdTests")
    public void testGetOfferRuleByIdTests(Test test) throws Exception {
        doReturn(Optional.ofNullable(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("existingOfferRule"),
                OfferRuleDetails.class))).when(offerRuleDetailsReadRepository).findById(anyLong());

            String ruleId = test.getThirdPartyMockResponse().get("ruleId");
        String url = "/loyalty/offer-rules/get/" + ruleId;
        doGetTest(test, mockMvc, url);
    }

    @ParameterizedTest
    @MethodSource("getOfferRulesTests")
    public void testGetOfferRulesTests(Test test) throws Exception {
        List<OfferRuleDetails> list = new ArrayList<>();
        list.add(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("existingOfferRule"), OfferRuleDetails.class));
        Page<OfferRuleDetails> page = new PageImpl<>(list);
        doReturn(page).when(offerRuleDetailsReadRepository).findAll(Mockito.isA(Pageable.class));
        String url = "/loyalty/offer-rules/get-page";
        doGetTest(test, mockMvc, url);
    }

    @ParameterizedTest
    @MethodSource("toggleOfferRuleStatus")
    public void testToggleStatus(Test test) throws Exception {
        doReturn(Optional.ofNullable(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("existingOfferRule"),
                OfferRuleDetails.class))).when(offerRuleDetailsReadRepository).findById(anyLong());

        String ruleId = test.getThirdPartyMockResponse().get("ruleId");
        String status = test.getThirdPartyMockResponse().get("status");
        String url = "/loyalty/offer-rules/toggle-status/" + ruleId + "/" + status;
        doTest(test, mockMvc, url);
    }

    @ParameterizedTest
    @MethodSource("getOfferDetailsByCustomer")
    public void testGetOfferDetailsByCustomer(Test test) throws Exception {
       doReturn(Arrays.asList(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("loyaltyCustomerMappingEntity"),
               LoyaltyCustomerOfferMapping[].class))).when(loyaltyCustomerOfferMappingReadRepository)
               .findByCustomerIdAndRunning(anyString(), any(Date.class));
       String customerId = test.getThirdPartyMockResponse().get("customerId");
       String url = "/loyalty/offer-rules/getOffers/" + customerId + "/";
       doGetTest(test, mockMvc, url);
    }

}