package com.nykaa.loyalty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nykaa.loyalty.AbstractTest;
import com.nykaa.loyalty.dto.Test;
import com.nykaa.loyalty.entity.OfferRuleDetails;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.jms.listener.JMSListener;
import com.nykaa.loyalty.repository.read.OfferRuleDetailsReadRepository;
import com.nykaa.loyalty.utils.CommonUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.Optional;

import static com.nykaa.loyalty.enums.ErrorCodes.OFFER_END_DATE_SURPASSED;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class StartLoyaltyOfferEventQueueTest extends AbstractTest {

    @MockBean
    private JMSListener jmsListener;

    @MockBean
    private OfferRuleDetailsReadRepository offerRuleDetailsReadRepository;

    @Autowired
    private ObjectMapper mapper;

    private static Test[] startLoyaltyOfferTest() throws IOException {
        return CommonUtils.loadTests("classpath:LoyaltyOfferStartEventQueueTest.json");
    }

    @ParameterizedTest
    @MethodSource("startLoyaltyOfferTest")
    public void testStartLoyaltyOfferEvent(Test test) throws Exception {
        OfferRuleDetails offerRuleDetails;
        Optional<OfferRuleDetails> offerRuleDetailsOptional;
        switch (test.getName()) {
            case "offer_rule_not_found":
                offerRuleDetailsOptional = Optional.empty();
                doReturn(offerRuleDetailsOptional).when(offerRuleDetailsReadRepository)
                        .findByIdAndIsActive(anyLong(), anyBoolean());
                doThrow(new LoyaltyException(ErrorCodes.OFFER_RULE_NOT_FOUND)).when(jmsListener)
                        .processLoyaltyOfferStartEvent(test.getInputJSON());
                break;
            default:
                offerRuleDetails = mapper.readValue(test.getThirdPartyMockResponse().get("offerRule"),
                        OfferRuleDetails.class);
                offerRuleDetailsOptional = Optional.of(offerRuleDetails);
                doReturn(offerRuleDetailsOptional).when(offerRuleDetailsReadRepository)
                        .findByIdAndIsActive(anyLong(), anyBoolean());
                doThrow(new LoyaltyException(OFFER_END_DATE_SURPASSED)).when(jmsListener)
                        .processLoyaltyOfferStartEvent(test.getInputJSON());
                break;
        }
        // positive cases and assertions remaining
    }
}
