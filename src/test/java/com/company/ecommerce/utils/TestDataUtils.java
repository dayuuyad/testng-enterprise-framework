package com.company.ecommerce.utils;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试数据生成工具
 */
public class TestDataUtils {

    private static final Logger logger = LoggerFactory.getLogger(TestDataUtils.class);
    private static final Faker faker = new Faker();

    /**
     * 生成用户测试数据
     */
    public Map<String, Object> generateUserData() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", faker.name().username());
        userData.put("email", faker.internet().emailAddress());
        userData.put("firstName", faker.name().firstName());
        userData.put("lastName", faker.name().lastName());
        userData.put("phone", faker.phoneNumber().cellPhone());
        userData.put("password", "Test@12345");

        logger.debug("生成用户测试数据: {}", userData);
        return userData;
    }

    /**
     * 生成产品测试数据
     */
    public Map<String, Object> generateProductData() {
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", faker.commerce().productName());
        productData.put("description", faker.lorem().sentence());
        productData.put("price", faker.number().randomDouble(2, 10, 1000));
        productData.put("sku", "SKU-" + faker.number().digits(8));
        productData.put("category", faker.commerce().department());

        logger.debug("生成产品测试数据: {}", productData);
        return productData;
    }

    /**
     * 生成地址测试数据
     */
    public Map<String, Object> generateAddressData() {
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("street", faker.address().streetAddress());
        addressData.put("city", faker.address().city());
        addressData.put("state", faker.address().state());
        addressData.put("zipCode", faker.address().zipCode());
        addressData.put("country", faker.address().country());

        return addressData;
    }

    /**
     * 生成唯一的测试数据标识
     */
    public String generateUniqueId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + faker.number().digits(4);
    }

    /**
     * 清理测试数据
     */
    public void cleanup() {
        logger.info("清理测试数据...");
        // 这里可以实现清理逻辑，比如删除临时文件等
    }
}