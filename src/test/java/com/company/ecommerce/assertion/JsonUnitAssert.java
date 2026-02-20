package com.company.ecommerce.assertion;

import org.testng.annotations.Test;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

public class JsonUnitAssert {

    /**
     * 严格比较：所有字段必须完全匹配
     */
    public static void assertStrictMatch(String actual, String expected) throws JSONException {
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    /**
     * 非严格比较：忽略数组顺序
     */
    public static void assertLenientMatch(String actual, String expected) throws JSONException {
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
    }

    /**
     * 自定义比较：可以忽略特定字段
     */
    public static void assertWithIgnoreFields(String actual, String expected, String... ignoreFields) throws JSONException {
        JSONComparator comparator = new CustomComparator(
                JSONCompareMode.STRICT
                // 这里可以添加自定义的比较规则
        );
        JSONAssert.assertEquals(expected, actual, comparator);
    }
}