package com.company.ecommerce.assertion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * 可自定义比较规则的Jackson断言工具
 */
public class JacksonFlexibleAssert {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static class JsonCompareOptions {
        private Set<String> ignoreFields = new HashSet<>();
        private Map<String, BiPredicate<JsonNode, JsonNode>> customComparators = new HashMap<>();
        private boolean ignoreArrayOrder = false;
        private boolean ignoreExtraFields = false;

        public JsonCompareOptions ignoreField(String fieldPath) {
            ignoreFields.add(fieldPath);
            return this;
        }

        public JsonCompareOptions withComparator(String fieldPath,
                                                 BiPredicate<JsonNode, JsonNode> comparator) {
            customComparators.put(fieldPath, comparator);
            return this;
        }

        public JsonCompareOptions ignoreArrayOrder() {
            this.ignoreArrayOrder = true;
            return this;
        }

        public JsonCompareOptions ignoreExtraFields() {
            this.ignoreExtraFields = true;
            return this;
        }
    }

    public static void assertEquals(String expectedJson, String actualJson,
                                    JsonCompareOptions options) throws Exception {
        JsonNode expected = objectMapper.readTree(expectedJson);
        JsonNode actual = objectMapper.readTree(actualJson);

        CompareResult result = compareNodes(expected, actual, "", options);

        if (!result.isMatch) {
            throw new AssertionError("JSON比较失败:\n" + result.message);
        }
    }

    private static CompareResult compareNodes(JsonNode expected, JsonNode actual,
                                              String path, JsonCompareOptions options) {
        // 检查是否忽略该字段
        if (options.ignoreFields.contains(path)) {
            return CompareResult.match();
        }

        // 检查是否有自定义比较器
        if (options.customComparators.containsKey(path)) {
            boolean match = options.customComparators.get(path).test(expected, actual);
            return match ? CompareResult.match() :
                    CompareResult.failure(path + ": 自定义比较失败");
        }

        // 基本类型比较
        if (expected.isValueNode() && actual.isValueNode()) {
            return expected.equals(actual) ? CompareResult.match() :
                    CompareResult.failure(String.format("%s: 值不匹配 - 预期: %s, 实际: %s",
                            path, expected, actual));
        }

        // 对象比较
        if (expected.isObject() && actual.isObject()) {
            return compareObjects((ObjectNode) expected, (ObjectNode) actual, path, options);
        }

        // 数组比较
        if (expected.isArray() && actual.isArray()) {
            return compareArrays((ArrayNode) expected, (ArrayNode) actual, path, options);
        }

        return CompareResult.failure(path + ": 类型不匹配");
    }

    private static CompareResult compareObjects(ObjectNode expected, ObjectNode actual,
                                                String path, JsonCompareOptions options) {
        List<String> errors = new ArrayList<>();

        // 检查预期中的所有字段
        Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode expectedValue = field.getValue();
            JsonNode actualValue = actual.get(fieldName);

            String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;

            if (actualValue == null) {
                errors.add(fieldPath + ": 字段缺失");
            } else {
                CompareResult result = compareNodes(expectedValue, actualValue, fieldPath, options);
                if (!result.isMatch) {
                    errors.add(result.message);
                }
            }
        }

        // 检查多余字段
        if (!options.ignoreExtraFields) {
            Iterator<Map.Entry<String, JsonNode>> actualFields = actual.fields();
            while (actualFields.hasNext()) {
                Map.Entry<String, JsonNode> field = actualFields.next();
                if (!expected.has(field.getKey())) {
                    errors.add((path.isEmpty() ? field.getKey() : path + "." + field.getKey()) +
                            ": 多余字段");
                }
            }
        }

        return errors.isEmpty() ? CompareResult.match() :
                CompareResult.failure(String.join("\n", errors));
    }

    private static CompareResult compareArrays(ArrayNode expected, ArrayNode actual,
                                               String path, JsonCompareOptions options) {
        if (!options.ignoreArrayOrder && expected.size() != actual.size()) {
            return CompareResult.failure(String.format("%s: 数组长度不匹配 - 预期: %d, 实际: %d",
                    path, expected.size(), actual.size()));
        }

        List<String> errors = new ArrayList<>();

        if (options.ignoreArrayOrder) {
            // 忽略顺序比较
            List<JsonNode> expectedList = new ArrayList<>();
            expected.forEach(expectedList::add);
            List<JsonNode> actualList = new ArrayList<>();
            actual.forEach(actualList::add);

            for (JsonNode expectedItem : expectedList) {
                boolean found = false;
                for (int j = 0; j < actualList.size(); j++) {
                    CompareResult result = compareNodes(expectedItem, actualList.get(j),
                            path + "[?]", options);
                    if (result.isMatch) {
                        found = true;
                        actualList.remove(j);
                        break;
                    }
                }
                if (!found) {
                    errors.add(path + ": 找不到匹配的元素: " + expectedItem);
                }
            }
        } else {
            // 按顺序比较
            for (int i = 0; i < expected.size(); i++) {
                CompareResult result = compareNodes(expected.get(i), actual.get(i),
                        path + "[" + i + "]", options);
                if (!result.isMatch) {
                    errors.add(result.message);
                }
            }
        }

        return errors.isEmpty() ? CompareResult.match() :
                CompareResult.failure(String.join("\n", errors));
    }

    private static class CompareResult {
        boolean isMatch;
        String message;

        static CompareResult match() {
            CompareResult result = new CompareResult();
            result.isMatch = true;
            return result;
        }

        static CompareResult failure(String message) {
            CompareResult result = new CompareResult();
            result.isMatch = false;
            result.message = message;
            return result;
        }
    }
}