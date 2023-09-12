package com.nykaa.loyalty.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.nykaa.loyalty.dto.Test;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.json.JsonParseException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CommonUtils {
    public static String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
    public static <T> T mapFromJson(String json, Class<T> clazz)
          throws JsonParseException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    public static void doTest(Test t, MockMvc mockMvc, String url) throws Exception {
        String inputJson = t.getInputJSON() == null ? "" : t.getInputJSON();
        MvcResult result = mockMvc.perform(post(url).params(getRequestParamsMap(t))
              .contentType("application/json")
              .content(inputJson))
              .andReturn();
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> resultMap = gson.fromJson(result.getResponse().getContentAsString(), mapType);
        filterKeys(resultMap, t.getIgnoredKeys());
        String resultJson = new Gson().toJson(resultMap);
        JSONAssert.assertEquals(resultJson,t.getOutputJSON(), JSONCompareMode.LENIENT);
    }

    public static void doGetTest(Test t, MockMvc mockMvc, String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url).params(getRequestParamsMap(t))).andReturn();
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> resultMap = gson.fromJson(result.getResponse().getContentAsString(), mapType);
        filterKeys(resultMap, t.getIgnoredKeys());
        String resultJson = new Gson().toJson(resultMap);
        JSONAssert.assertEquals(resultJson,t.getOutputJSON(), JSONCompareMode.LENIENT);
    }

    public static Map<String, String> convertJsonStrToMap(String jsonStr) {
        Map<String, String> result = new HashMap<>();
        if (null == jsonStr) {
            return result;
        }

        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(jsonStr, type);
    }



    private static void filterKeys(Map<String, Object> keyMap, List<String> ignoreList) {
        if (!(CollectionUtils.isEmpty(keyMap) || CollectionUtils.isEmpty(ignoreList))) {
            ignoreList.stream().parallel().forEach(key -> recursiveRemove(keyMap, key));
        }
    }

    private static void recursiveRemove(Map<String, Object> keyMap, String key) {
        List<String> path = Arrays.asList(StringUtils.split(key.trim(), "."));
        int size = path.size();
        int index = 0;
        List<LinkedTreeMap> treeMapList = new ArrayList<LinkedTreeMap>();
        treeMapList.add((LinkedTreeMap) keyMap);
        while (index != size - 1) {
            int i = index++;
            List<LinkedTreeMap> treeMapListTemp = new ArrayList<LinkedTreeMap>();
            treeMapList.stream().parallel().forEach(treeMap -> {
                Object obj = treeMap.get(path.get(i));
                if (obj instanceof List) {
                    treeMapListTemp.addAll((List<LinkedTreeMap>) obj);
                } else if (obj instanceof LinkedTreeMap) {
                    treeMapListTemp.add((LinkedTreeMap) obj);
                }
            });
            treeMapList = treeMapListTemp;
        }
        treeMapList.stream().parallel().forEach(treeMap -> treeMap.remove(path.get(size - 1)));
    }

    public static Test[] loadTests(String fileName) throws IOException {
        File file = ResourceUtils.getFile(fileName);
        String testString = new String(Files.readAllBytes(file.toPath()));
        Test[] tests= new Test[1000];
        tests=CommonUtils.mapFromJson(testString,tests.getClass());
        return tests;
    }

    public static LinkedMultiValueMap<String, String> getRequestParamsMap(Test t) {
        LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        if (null == t || null == t.getRequestParams()) {
            return requestParams;
        }

        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> inputMap = new Gson().fromJson(t.getRequestParams(), type);
        for (String key : inputMap.keySet()) {
            requestParams.add(key, inputMap.get(key));
        }

        return requestParams;
    }

    public static com.nykaa.loyalty.dto.CucumberTest loadInput(String fileName) throws IOException {
        File file = ResourceUtils.getFile(fileName);
        String testString = new String(Files.readAllBytes(file.toPath()));
        com.nykaa.loyalty.dto.CucumberTest tests = new com.nykaa.loyalty.dto.CucumberTest();
        tests=CommonUtils.mapFromJson(testString, tests.getClass());
        return tests;
    }
}
