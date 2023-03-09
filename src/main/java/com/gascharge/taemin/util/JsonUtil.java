package com.gascharge.taemin.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

import static com.gascharge.taemin.common.object.JavaTypeUtil.getJavaType;


@Slf4j
public class JsonUtil {
    public static String setJson(Object data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error(String.valueOf(e));
            throw e;
        }
    }

    public static Object getJson(String data, Type javaType) throws JacksonException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data, getJavaType(javaType));
        } catch (JacksonException e) {
            log.error(String.valueOf(e));
            throw e;
        }
    }
}
