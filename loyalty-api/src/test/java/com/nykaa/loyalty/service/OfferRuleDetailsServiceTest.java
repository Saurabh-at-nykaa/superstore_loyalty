package com.nykaa.loyalty.service;

import com.google.gson.Gson;
import com.nykaa.loyalty.AbstractTest;
import com.nykaa.loyalty.dto.OfferRuleDetailsDto;
import com.nykaa.loyalty.dto.Test;
import com.nykaa.loyalty.entity.OfferRuleDetails;
import com.nykaa.loyalty.repository.read.OfferRuleDetailsReadRepository;
import com.nykaa.loyalty.utils.CommonUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class OfferRuleDetailsServiceTest extends AbstractTest {

    private static Test[] validateAndGetQueryCount() throws IOException {
        return CommonUtils.loadTests("classpath:OfferRulesController_validateAndGetQueryCount.json");
    }

    @Autowired
    OfferRuleDetailsService offerRuleDetailsService;

    @MockBean
    private OfferRuleDetailsReadRepository offerRuleDetailsReadRepository;

    @ParameterizedTest
    @MethodSource("validateAndGetQueryCount")
    public void validateAndGetQueryCount(Test test) throws Exception {
        doReturn(Optional.ofNullable(CommonUtils.mapFromJson(test.getThirdPartyMockResponse().get("existingOfferRule"),
                OfferRuleDetails.class))).when(offerRuleDetailsReadRepository)
                .findByDomainAndOfferTypeAndStartDateAndEndDateAndBrandShareAndNykaaShareAndCustomerType(any(),
                        any(), any(), any(), any(), any(), any());

        OfferRuleDetailsDto input = CommonUtils.mapFromJson(test.getInputJSON(), OfferRuleDetailsDto.class);
        Map<String, Object> result = offerRuleDetailsService.validateAndGetQueryCount(input);
        String resultJson = new Gson().toJson(result);
        JSONAssert.assertEquals(resultJson,test.getOutputJSON(), JSONCompareMode.LENIENT);
    }

}
