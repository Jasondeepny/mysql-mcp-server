package com.zcckj.mcp.mysql.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>JSON工具类</p>
 *
 * @author zhangjie
 * @since 2018-08-01 09:53
 */
@Slf4j
public class JsonUtils {
    /**
     * 线程安全
     */
    private static final ObjectMapper MAPPER;
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    static {
        MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        /**number转换为String类型展示,防止精度丢失的问题*/
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        MAPPER.registerModule(simpleModule);

        /**统一设置日期格式*/
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        MAPPER.setDateFormat(format);
        /*忽略不存在的字段*/
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtils() {
    }

    public static String toJsonString(Object object) {
        try {
            if (object == null) {
                return null;
            }
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        Assert.isTrue(!StringUtils.hasText(json), "json cannot be empty");
        Assert.notNull(clazz, "clazz cannot be null");
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, E> Map<T, E> toMap(String json, Class<T> clazz, Class<E> eClass) {
        try {
            if (StringUtils.hasText(json)) {
                return null;
            }
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(Map.class, clazz, eClass);
            Map<T, E> map = MAPPER.readValue(json, javaType);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        try {
            if (StringUtils.hasText(json)) {
                return null;
            }
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(ArrayList.class, clazz);
            List<T> list = MAPPER.readValue(json, javaType);
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象转换为JSON流
     *
     * @param writer writer
     * @param value  对象
     */
    public static void writeValue(Writer writer, Object value) {
        try {
            MAPPER.writeValue(writer, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
