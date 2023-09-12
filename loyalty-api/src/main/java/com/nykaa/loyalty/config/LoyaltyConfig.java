package com.nykaa.loyalty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LoyaltyConfig {

    @Bean("loyaltyObjectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean("loyaltyRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
