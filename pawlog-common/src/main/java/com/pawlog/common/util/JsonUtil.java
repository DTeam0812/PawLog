package com.pawlog.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.JsonNode;
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static JsonNode toJsonNode(Object object) {
    return objectMapper.valueToTree(object);
    }   // JSON분리
    static {
        // LocalDate, LocalDateTime 등 날짜 타입 변환 등록
        objectMapper.registerModule(new JavaTimeModule());
    }

    // 객체 -> JSON 문자열
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    // JSON 문자열 -> 객체
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 역직렬화 실패", e);
        }
    }
}