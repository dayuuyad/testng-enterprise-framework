package com.company.ecommerce.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 启用漂亮的格式化输出
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 处理日期格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 处理空对象
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static String toPrettyJson(Object object) {
        if (object == null) {
            return "null";
        }

        try {
            // 如果是字符串，先尝试解析为JSON对象再格式化
            if (object instanceof String) {
                try {
                    Object json = objectMapper.readValue((String) object, Object.class);
                    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                } catch (Exception e) {
                    // 不是有效的JSON字符串，直接返回原字符串
                    return (String) object;
                }
            }

            // 普通对象转换为格式化JSON
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);
        } catch (Exception e) {
            // 转换失败时返回对象的toString()
            return "Failed to convert to JSON: " + object.toString();
        }
    }

    // 重载方法，支持自定义配置
    public static String toPrettyJson(Object object, boolean includeNulls) {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(SerializationFeature.INDENT_OUTPUT);

        if (!includeNulls) {
            customMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        try {
            return customMapper.writeValueAsString(object);
        } catch (Exception e) {
            return object.toString();
        }
    }


    public static String responseToPrettyJson(Response response) {
        try {
            // 构建包含完整响应信息的对象
            Map<String, Object> responseInfo = new LinkedHashMap<>();

            // 1. 状态行信息
//            responseInfo.put("statusCode", response.getStatusCode());
//            responseInfo.put("statusLine", response.getStatusLine());
//            responseInfo.put("contentType", response.getContentType());

            // 2. 响应头
            Map<String, String> headers = new LinkedHashMap<>();
            response.getHeaders().forEach(header ->
                    headers.put(header.getName(), header.getValue())
            );
//            responseInfo.put("headers", headers);

            // 3. 响应体
            String body = response.getBody().asString();
            if (body != null && !body.isEmpty()) {
                // 尝试解析为 JSON
                try {
                    Object jsonBody = objectMapper.readValue(body, Object.class);
                    responseInfo.put("body", jsonBody);
                } catch (Exception e) {
                    // 如果不是 JSON，直接存字符串
                    responseInfo.put("body", body);
                }
            } else {
                responseInfo.put("body", "empty");
            }

            // 4. 响应时间
            responseInfo.put("responseTime", response.getTime() + " ms");

            // 5. Cookies
            if (!response.getCookies().isEmpty()) {
                responseInfo.put("cookies", response.getCookies());
            }

            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(responseInfo);

        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}