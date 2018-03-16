package com.ft.graphql.utill;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * This example code chose to use GSON as its JSON parser. Any JSON parser should be fine
 */
public class JsonKit {
    static final ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> toMap(String jsonStr) {

        if (jsonStr == null || jsonStr.trim().length() == 0) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(jsonStr, new TypeReference<Map<String,Object>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map == null ? Collections.emptyMap() : map;
    }
}
