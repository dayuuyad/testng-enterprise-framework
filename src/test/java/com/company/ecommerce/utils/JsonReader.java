// src/main/java/com/company/ecommerce/utils/JsonReader.java
package com.company.ecommerce.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JsonReader {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T[][] readTestData(String fileName, Class<T[]> clazz) {
        try {
            String filePath = System.getProperty("user.dir") + "/src/test/resources/testdata/" + fileName;
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            // 读取为数组
            T[] array = mapper.readValue(content, clazz);

            // 转换为 Object[][]
            @SuppressWarnings("unchecked")
            T[][] result = (T[][]) java.lang.reflect.Array.newInstance(clazz.getComponentType(), array.length, 0);

            for (int i = 0; i < array.length; i++) {
                result[i] = (T[]) java.lang.reflect.Array.newInstance(clazz.getComponentType().getComponentType(), 1);
                result[i][0] = array[i];
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + fileName, e);
        }
    }

    // 另一种方式，直接读取为 List 并转换为数组
    public static <T> Object[][] readTestDataAsArray(String fileName, Class<T> clazz) {
        try {
            String filePath = System.getProperty("user.dir") + "/src/test/resources/testdata/" + fileName;
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            List<T> list = mapper.readValue(content, new TypeReference<List<T>>() {});

            Object[][] result = new Object[list.size()][1];
            for (int i = 0; i < list.size(); i++) {
                result[i][0] = list.get(i);
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + fileName, e);
        }
    }
}