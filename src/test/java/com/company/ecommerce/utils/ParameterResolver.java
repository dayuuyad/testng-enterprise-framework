package com.company.ecommerce.utils;

import com.company.ecommerce.utils.testdata.TestDataUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterResolver {
    // 占位符模式：${变量名}
    private static final Pattern CONTEXT_PLACEHOLDER_PATTERN =
            Pattern.compile("\\$\\{([^}]+)\\}");
    // 占位符模式：#{方法名}
    private static final Pattern METHOD_PLACEHOLDER_PATTERN =
            Pattern.compile("\\#\\{([^}]+)\\}");
    private Map<String, Object> context;

    // 构造器注入
    public ParameterResolver(Map<String, Object> context) {
        this.context = context;
    }


    /**
     * 解析字符串中的所有占位符
     * 支持：${userId} 从上下文获取
     * 支持：#{uuid} 调用方法生成
     */
    public JsonNode requestResolve(String parameter) {
        if (parameter == null || parameter.isEmpty()) {
//            return parameter;
            return null;
        }

        String result = parameter;

        // 1. 先处理方法调用 #{methodName}
        result = resolveMethodCalls(result);

        // 2. 再处理上下文变量 ${variableName}
        result = resolveContextVariables(result);

        try {
            return  new ObjectMapper().readTree(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 解析字符串中的所有占位符
     * 支持：${userId} 从上下文获取
     * 支持：#{uuid} 调用方法生成
     */
    public String sqlResolve(String parameter) {
        if (parameter == null || parameter.isEmpty()) {
//            return parameter;
            return null;
        }
        return  resolveContextVariables(parameter);
    }



    /**
     * 从响应中提取值并存入上下文
     */
    public void responseSave (String responseExtracts, Response response) {

        Map<String, String> extracts = parseResponseExtracts(responseExtracts);

        for (Map.Entry<String, String> extract : extracts.entrySet()) {
            String varName = extract.getKey();      // 变量名
            String jsonPath = extract.getValue();   // JSON路径

            Object value = response.jsonPath().get(jsonPath);
            if (value != null) {
                context.put(varName, value);
                System.out.println("提取值: " + varName + " = " + value +
                        " (来自: " + jsonPath + ")");
            }
        }
    }

    /**
     * 解析返回值提取映射
     */
    private Map<String, String> parseResponseExtracts(String responseExtracts) {
        Map<String, String> extractMap = new HashMap<>();
        if (responseExtracts != null && !responseExtracts.isEmpty()) {
            String[] extracts = responseExtracts.split(",");
            for (String extract : extracts) {
                String[] parts = extract.trim().split(":");
                if (parts.length == 2) {
                    extractMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return extractMap;
    }

    /**
     * 处理方法调用 #{methodName}
     */
    private String resolveMethodCalls(String input) {
        Matcher matcher = METHOD_PLACEHOLDER_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String methodName = matcher.group(1);
            String generatedValue = TestDataUtils.generateValue(methodName);
            matcher.appendReplacement(sb,
                    Matcher.quoteReplacement(generatedValue));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 处理上下文变量 ${variableName}
     */
    private String resolveContextVariables(String input) {
        Matcher matcher = CONTEXT_PLACEHOLDER_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = context.get(varName);
            if (value == null) {
                throw new RuntimeException("上下文不存在变量: " + varName);
            }
            matcher.appendReplacement(sb,
                    Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
