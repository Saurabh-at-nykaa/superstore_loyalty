package com.nykaa.loyalty.utils;


public class BuildingTestData {

    public static String buildTestDataForSuperstoreAggregator(String key,String endpoint) throws Exception
    {
        String data_key = key + '_' + endpoint;
        com.nykaa.loyalty.dto.CucumberTest t = CommonUtils.loadInput("classpath:CucumberTestCases.json");
        return t.getTestData().get(data_key);

    }
}