package com.nykaa.loyalty.controller;

import com.nykaa.loyalty.AbstractTest;
import com.nykaa.loyalty.dto.Test;
import com.nykaa.loyalty.service.impl.LoyaltyStartupServiceImpl;
import com.nykaa.loyalty.utils.CommonUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static com.nykaa.loyalty.utils.CommonUtils.doGetTest;

public class LoyaltyCacheControllerTest extends AbstractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private LoyaltyStartupServiceImpl startupService;

    private static Test[] CacheController_loadCache() throws IOException {
        return CommonUtils.loadTests("classpath:CacheController_loadCache.json");
    }

    @ParameterizedTest
    @MethodSource("CacheController_loadCache")
    public void testLoadCache(com.nykaa.loyalty.dto.Test testData) throws Exception {

        // Call endpoint
        String url = "/loyalty/loadCache";
        doGetTest(testData, mockMvc, url);
    }
}
