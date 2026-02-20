package com.company.ecommerce.assertion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 增强版智能JSON断言工具
 * 支持在预期JSON中使用标识符进行灵活验证
 */
public class EnhancedSmartJsonAssert {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 基础标识符
    public static final String IGNORE = "${ignore}";           // 忽略该字段
    public static final String NOT_NULL = "${notnull}";        // 非空
    public static final String NULL = "${null}";               // 必须为null
    public static final String NOT_EMPTY = "${notempty}";      // 非空字符串/数组/对象
    public static final String IS_NUMBER = "${isnumber}";      // 必须是数字
    public static final String IS_STRING = "${isstring}";      // 必须是字符串
    public static final String IS_BOOLEAN = "${isboolean}";    // 必须是布尔值
    public static final String IS_ARRAY = "${isarray}";        // 必须是数组
    public static final String IS_OBJECT = "${isobject}";      // 必须是对象

    // 格式验证标识符
    public static final String IS_EMAIL = "${isEmail}";        // 邮箱格式
    public static final String IS_PHONE = "${isPhone}";        // 手机号格式
    public static final String IS_ID_CARD = "${isIdCard}";     // 身份证格式
    public static final String IS_URL = "${isUrl}";            // URL格式
    public static final String IS_IP = "${isIp}";              // IP地址格式
    public static final String IS_UUID = "${isUuid}";          // UUID格式
    public static final String IS_DATE = "${isDate}";          // 日期格式 (yyyy-MM-dd)
    public static final String IS_DATETIME = "${isDatetime}";  // 日期时间格式 (yyyy-MM-dd HH:mm:ss)
    public static final String IS_TIMESTAMP = "${isTimestamp}"; // 时间戳格式 (13位数字)

    // 带参数的标识符前缀
    public static final String MATCHES = "${matches:";         // 正则匹配，如 ${matches:^[A-Z]+$}
    public static final String IN = "${in:";                   // 枚举值，如 ${in:1,2,3}
    public static final String GT = "${gt:";                   // 大于，如 ${gt:0}
    public static final String LT = "${lt:";                   // 小于，如 ${lt:100}
    public static final String GTE = "${gte:";                 // 大于等于
    public static final String LTE = "${lte:";                 // 小于等于
    public static final String LENGTH = "${length:";           // 长度范围，如 ${length:5,10}
    public static final String RANGE = "${range:";             // 数值范围，如 ${range:1,100}

    // 预编译正则表达式
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_CARD_PATTERN =
            Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    private static final Pattern IP_PATTERN =
            Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern DATETIME_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    private static final Pattern TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{13}$");

    /**
     * 智能断言：根据预期JSON中的标识符进行比较
     * @param actualJson 实际返回的JSON字符串
     * @param expectedJson 包含标识符的预期JSON字符串
     */
    public static void smartAssert(String actualJson, String expectedJson) throws Exception {
        // 解析实际响应为JsonNode用于分析
        JsonNode actualNode = objectMapper.readTree(actualJson);

        // 解析预期响应中的标识符，收集自定义验证规则
        Map<String, CustomValidation> validations = new HashMap<>();
        collectValidations(actualNode, expectedJson, "", validations);

        // 构建Customization数组
        List<Customization> customizations = new ArrayList<>();
        for (Map.Entry<String, CustomValidation> entry : validations.entrySet()) {
            String fieldPath = entry.getKey();
            CustomValidation validation = entry.getValue();

            customizations.add(Customization.customization(fieldPath,
                    (actual, expected) -> validation.validate(actual)));
        }

        // 移除标识符，得到纯净的预期JSON用于结构验证
        String cleanExpectedJson = removeMarkers(expectedJson);

        // 执行断言
        CustomComparator comparator = new CustomComparator(
                JSONCompareMode.STRICT,
                customizations.toArray(new Customization[0])
        );

        JSONAssert.assertEquals(cleanExpectedJson, actualJson, comparator);
    }

    /**
     * 递归收集所有需要自定义验证的字段
     */
    private static void collectValidations(JsonNode actualNode, String expectedJson,
                                           String path, Map<String, CustomValidation> validations) throws IOException {
        JsonNode expectedNode = objectMapper.readTree(expectedJson);
        collectValidationsFromNode(actualNode, expectedNode, path, validations);
    }

    private static void collectValidationsFromNode(JsonNode actualNode, JsonNode expectedNode,
                                                   String path, Map<String, CustomValidation> validations) {
        if (expectedNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = expectedNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode expectedValue = field.getValue();
                JsonNode actualValue = actualNode != null ? actualNode.get(fieldName) : null;

                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;

                if (expectedValue.isTextual()) {
                    String textValue = expectedValue.asText();
                    if (isMarker(textValue)) {
                        CustomValidation validation = createValidation(textValue);
                        if (validation != null) {
                            validations.put(currentPath, validation);
                            continue;
                        }
                    }
                }

                // 递归处理子对象
                if (actualValue != null && expectedValue != null) {
                    collectValidationsFromNode(actualValue, expectedValue, currentPath, validations);
                }
            }
        } else if (expectedNode.isArray()) {
            for (int i = 0; i < expectedNode.size(); i++) {
                if (actualNode != null && actualNode.isArray() && i < actualNode.size()) {
                    collectValidationsFromNode(actualNode.get(i), expectedNode.get(i),
                            path + "[" + i + "]", validations);
                }
            }
        }
    }

    /**
     * 判断字符串是否为标识符
     */
    private static boolean isMarker(String text) {
        return text.startsWith("${") && text.endsWith("}");
    }

    /**
     * 根据标识符创建验证规则
     */
    private static CustomValidation createValidation(String marker) {
        switch (marker) {
            case IGNORE:
                return actual -> true;

            case NOT_NULL:
                return actual -> actual != null;

            case NULL:
                return actual -> actual == null;

            case NOT_EMPTY:
                return actual -> {
                    if (actual == null) return false;
                    String str = actual.toString();
                    return !str.isEmpty() && !"{}".equals(str) && !"[]".equals(str);
                };

            case IS_NUMBER:
                return actual -> actual instanceof Number;

            case IS_STRING:
                return actual -> actual instanceof String;

            case IS_BOOLEAN:
                return actual -> actual instanceof Boolean;

            case IS_ARRAY:
                return actual -> actual instanceof JSONArray;

            case IS_OBJECT:
                return actual -> actual instanceof JSONObject;

            case IS_EMAIL:
                return actual -> actual != null &&
                        EMAIL_PATTERN.matcher(actual.toString()).matches();

            case IS_PHONE:
                return actual -> actual != null &&
                        PHONE_PATTERN.matcher(actual.toString()).matches();

            case IS_ID_CARD:
                return actual -> actual != null &&
                        ID_CARD_PATTERN.matcher(actual.toString()).matches();

            case IS_URL:
                return actual -> actual != null &&
                        URL_PATTERN.matcher(actual.toString()).matches();

            case IS_IP:
                return actual -> actual != null &&
                        IP_PATTERN.matcher(actual.toString()).matches();

            case IS_UUID:
                return actual -> actual != null &&
                        UUID_PATTERN.matcher(actual.toString()).matches();

            case IS_DATE:
                return actual -> actual != null &&
                        DATE_PATTERN.matcher(actual.toString()).matches();

            case IS_DATETIME:
                return actual -> actual != null &&
                        DATETIME_PATTERN.matcher(actual.toString()).matches();

            case IS_TIMESTAMP:
                return actual -> actual != null &&
                        TIMESTAMP_PATTERN.matcher(actual.toString()).matches();
        }

        // 处理带参数的标识符
        if (marker.startsWith(MATCHES)) {
            String regex = marker.substring(MATCHES.length(), marker.length() - 1);
            return actual -> actual != null && actual.toString().matches(regex);
        }

        if (marker.startsWith(IN)) {
            String values = marker.substring(IN.length(), marker.length() - 1);
            String[] allowed = values.split(",");
            Set<String> allowedSet = new HashSet<>(Arrays.asList(allowed));
            return actual -> actual != null && allowedSet.contains(actual.toString());
        }

        if (marker.startsWith(GT)) {
            double threshold = Double.parseDouble(marker.substring(GT.length(), marker.length() - 1));
            return actual -> {
                if (actual == null) return false;
                try {
                    return Double.parseDouble(actual.toString()) > threshold;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        }

        if (marker.startsWith(LT)) {
            double threshold = Double.parseDouble(marker.substring(LT.length(), marker.length() - 1));
            return actual -> {
                if (actual == null) return false;
                try {
                    return Double.parseDouble(actual.toString()) < threshold;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        }

        if (marker.startsWith(GTE)) {
            double threshold = Double.parseDouble(marker.substring(GTE.length(), marker.length() - 1));
            return actual -> {
                if (actual == null) return false;
                try {
                    return Double.parseDouble(actual.toString()) >= threshold;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        }

        if (marker.startsWith(LTE)) {
            double threshold = Double.parseDouble(marker.substring(LTE.length(), marker.length() - 1));
            return actual -> {
                if (actual == null) return false;
                try {
                    return Double.parseDouble(actual.toString()) <= threshold;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        }

        if (marker.startsWith(LENGTH)) {
            String range = marker.substring(LENGTH.length(), marker.length() - 1);
            String[] parts = range.split(",");
            int min = Integer.parseInt(parts[0]);
            int max = parts.length > 1 ? Integer.parseInt(parts[1]) : min;
            return actual -> {
                if (actual == null) return false;
                int len = actual.toString().length();
                return len >= min && len <= max;
            };
        }

        if (marker.startsWith(RANGE)) {
            String range = marker.substring(RANGE.length(), marker.length() - 1);
            String[] parts = range.split(",");
            double min = Double.parseDouble(parts[0]);
            double max = Double.parseDouble(parts[1]);
            return actual -> {
                if (actual == null) return false;
                try {
                    double value = Double.parseDouble(actual.toString());
                    return value >= min && value <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        }

        return null;
    }

    /**
     * 移除JSON中的所有标识符，返回纯净的JSON用于结构验证
     */
    private static String removeMarkers(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode cleaned = removeMarkersFromNode(root);
        return objectMapper.writeValueAsString(cleaned);
    }

    private static JsonNode removeMarkersFromNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode value = field.getValue();

                if (value.isTextual() && isMarker(value.asText())) {
                    // 根据标识符类型设置合适的默认值
                    String marker = value.asText();
                    if (marker.equals(IS_ARRAY) || marker.equals(NOT_EMPTY) || marker.equals(IGNORE)) {
                        objectNode.putArray(fieldName);
                    } else if (marker.equals(IS_OBJECT)) {
                        objectNode.putObject(fieldName);
                    } else if (marker.equals(IS_NUMBER) || marker.startsWith(RANGE) ||
                            marker.startsWith(GT) || marker.startsWith(GTE) ||
                            marker.startsWith(LT) || marker.startsWith(LTE)) {
                        objectNode.put(fieldName, 0);
                    } else if (marker.equals(IS_BOOLEAN)) {
                        objectNode.put(fieldName, false);
                    } else if (marker.equals(NULL)) {
                        objectNode.putNull(fieldName);
                    } else {
                        objectNode.put(fieldName, "");
                    }
                } else {
                    objectNode.set(fieldName, removeMarkersFromNode(value));
                }
            }
            return objectNode;
        } else if (node.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode arrayNode = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                arrayNode.add(removeMarkersFromNode(item));
            }
            return arrayNode;
        }
        return node;
    }

    /**
     * 自定义验证函数接口
     */
    @FunctionalInterface
    private interface CustomValidation {
        boolean validate(Object actualValue);
    }

    // ==================== 便捷方法 ====================

    /**
     * 从文件加载预期JSON并断言
     */
    public static void assertWithExpectedFile(String actualJson, String expectedFilePath) throws Exception {
        String expectedJson = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(expectedFilePath)));
        smartAssert(actualJson, expectedJson);
    }

    /**
     * 从resources目录加载预期JSON并断言
     */
    public static void assertWithResource(String actualJson, String resourcePath) throws Exception {
        String expectedJson = new String(
                EnhancedSmartJsonAssert.class.getClassLoader().getResourceAsStream(resourcePath).readAllBytes());
        smartAssert(actualJson, expectedJson);
    }

    /**
     * 使用自定义比较模式断言
     */
    public static void smartAssert(String actualJson, String expectedJson,
                                   JSONCompareMode mode) throws Exception {
        // 这里简化处理，实际可以扩展支持不同模式
        smartAssert(actualJson, expectedJson);
    }
}