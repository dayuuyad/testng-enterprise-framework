package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
import com.company.ecommerce.reporters.AllureManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API 测试工具类 - 集成 Allure
 */
public class APIUtils {

    private static final Logger logger = LoggerFactory.getLogger(APIUtils.class);
    private RequestSpecification requestSpec;
    private String baseUrl;

    public APIUtils() {
        initialize();
    }

    private void initialize() {
        this.baseUrl = ConfigManager.getApiBaseUrl();

        // 配置 RestAssured
        RestAssured.baseURI = baseUrl;
        RestAssured.useRelaxedHTTPSValidation();

        // 创建请求规范，集成 Allure
        requestSpec = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("User-Agent", "TestNG-Automation-Framework")
                .filter(new AllureRestAssured()) // 关键：添加 Allure 过滤器
                .log().ifValidationFails();

        // 添加认证头
        String authToken = ConfigManager.getProperty("api.auth.token");
        if (authToken != null && !authToken.isEmpty()) {
            requestSpec.header("Authorization", "Bearer " + authToken);
        }

        logger.info("✅ API工具初始化完成，Base URL: {}", baseUrl);
    }

    public Response get(String endpoint) {
        return AllureManager.addManualStep("GET请求: " + endpoint, () ->
                requestSpec.when().get(endpoint)
        );
    }

    public Response get(String endpoint, Map<String, ?> queryParams) {
        return AllureManager.addManualStep("GET请求(带参数): " + endpoint, () ->
                requestSpec.queryParams(queryParams).when().get(endpoint)
        );
    }

    public Response post(String endpoint, Object body) {
        return AllureManager.addManualStep("POST请求: " + endpoint, () ->
                requestSpec.body(body).when().post(endpoint)
        );
    }

    public Response put(String endpoint, Object body) {
        return AllureManager.addManualStep("PUT请求: " + endpoint, () ->
                requestSpec.body(body).when().put(endpoint)
        );
    }

    public Response delete(String endpoint) {
        return AllureManager.addManualStep("DELETE请求: " + endpoint, () ->
                requestSpec.when().delete(endpoint)
        );
    }

    public Response patch(String endpoint, Object body) {
        return AllureManager.addManualStep("PATCH请求: " + endpoint, () ->
                requestSpec.body(body).when().patch(endpoint)
        );
    }

    /**
     * 关闭资源
     */
    public void close() {
        // 目前没有需要关闭的资源，预留方法
        logger.info("API 工具已关闭");
    }
}