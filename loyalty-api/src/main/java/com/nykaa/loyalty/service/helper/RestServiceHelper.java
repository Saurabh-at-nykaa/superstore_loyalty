package com.nykaa.loyalty.service.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nykaa.loyalty.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestServiceHelper {

    @Qualifier("loyaltyRestTemplate") private final RestTemplate restTemplate;

    @Qualifier("loyaltyObjectMapper") private final ObjectMapper objectMapper;

    public JsonNode getForData(String url) throws JsonMappingException, JsonProcessingException {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            log.error("failed to fetch response for url : {}. Got status code {} in response", url, responseEntity.getStatusCode());
            return null;
        }
        JsonNode response = objectMapper.readTree(responseEntity.getBody());
        if (!response.get(Constants.SUCCESS).asBoolean()
                || !response.get(Constants.STATUS_CODE).asText().equals(Constants.OK_STATUS_CODE)) {
            log.error(
                    "error in response for url : {}. Got success {} & status code {} in response", url,
                    response.get(Constants.SUCCESS).asBoolean(), response.get(Constants.STATUS_CODE).asText());
            return null;
        }
        return response.get(Constants.DATA);
    }

}
