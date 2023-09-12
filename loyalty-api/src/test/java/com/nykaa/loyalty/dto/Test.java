package com.nykaa.loyalty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test {
    private String name;
    private String inputJSON;
    private String requestParams;
    private String pathParams;
    private String outputJSON;
    private ArrayList<String> ignoredKeys;
    private Map<String, String> thirdPartyMockResponse;

}

