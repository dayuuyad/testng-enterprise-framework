package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API 测试工具类
 * 提供 REST API 测试的便捷方法
 */
public class APIUtils {

    private static final Logger logger = LoggerFactory.getLogger(APIUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private RequestSpecification requestSpec;
    private String baseUrl;
    private Map<String, String> defaultHeaders;
    private Map<String, String> defaultCookies;

    /**
     * 构造函数
     */
    public APIUtils() {
        initialize();
    }

    /**
     * 初始化 API 客户端
     */
    private void initialize() {
        this.baseUrl = ConfigManager.getApiBaseUrl();
        this.defaultHeaders = new HashMap<>();
        this.defaultCookies = new HashMap<>();

        // 设置默认配置
        RestAssured.baseURI = baseUrl;
        RestAssured.useRelaxedHTTPSValidation(); // 宽松的 HTTPS 验证

        // 配置默认请求规范
        requestSpec = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("User-Agent", "TestNG-Automation-Client/1.0")
                .header("X-Requested-With", "XMLHttpRequest");

        // 添加认证头（如果配置了）
        String authToken = ConfigManager.getProperty("api.auth.token");
        if (authToken != null && !authToken.isEmpty()) {
            requestSpec.header("Authorization", "Bearer " + authToken);
        }

        // 配置超时
        int timeout = ConfigManager.getIntProperty("api.timeout", 30);
        RestAssured.config = RestAssured.config()
                .httpClient(RestAssured.config().getHttpClientConfig()
                        .setParam("http.connection.timeout", timeout * 1000)
                        .setParam("http.socket.timeout", timeout * 1000));

        logger.info("✅ API 工具初始化完成，Base URL: {}", baseUrl);
    }

    /**
     * 发送 GET 请求
     */
    public Response get(String endpoint) {
        return get(endpoint, null, null);
    }

    public Response get(String endpoint, Map<String, ?> queryParams) {
        return get(endpoint, queryParams, null);
    }

    public Response get(String endpoint, Map<String, ?> queryParams, Map<String, String> headers) {
        return executeRequest("GET", endpoint, null, queryParams, headers, null);
    }

    /**
     * 发送 POST 请求
     */
    public Response post(String endpoint, Object body) {
        return post(endpoint, body, null, null);
    }

    public Response post(String endpoint, Object body, Map<String, String> headers) {
        return post(endpoint, body, null, headers);
    }

    public Response post(String endpoint, Object body, Map<String, ?> queryParams, Map<String, String> headers) {
        return executeRequest("POST", endpoint, body, queryParams, headers, null);
    }

    /**
     * 发送 PUT 请求
     */
    public Response put(String endpoint, Object body) {
        return put(endpoint, body, null);
    }

    public Response put(String endpoint, Object body, Map<String, String> headers) {
        return executeRequest("PUT", endpoint, body, null, headers, null);
    }

    /**
     * 发送 DELETE 请求
     */
    public Response delete(String endpoint) {
        return delete(endpoint, null);
    }

    public Response delete(String endpoint, Map<String, String> headers) {
        return executeRequest("DELETE", endpoint, null, null, headers, null);
    }

    /**
     * 发送 PATCH 请求
     */
    public Response patch(String endpoint, Object body) {
        return patch(endpoint, body, null);
    }

    public Response patch(String endpoint, Object body, Map<String, String> headers) {
        return executeRequest("PATCH", endpoint, body, null, headers, null);
    }

    /**
     * 执行请求的核心方法
     */
    private Response executeRequest(String method, String endpoint, Object body,
                                    Map<String, ?> queryParams, Map<String, String> headers,
                                    Map<String, String> cookies) {

        String url = buildUrl(endpoint);
        logger.info("🌐 {} {}", method, url);

        RequestSpecification spec = given()
                .spec(requestSpec)
                .urlEncodingEnabled(false); // 禁用 URL 编码

        // 添加查询参数
        if (queryParams != null && !queryParams.isEmpty()) {
            spec.queryParams(queryParams);
            logger.debug("查询参数: {}", queryParams);
        }

        // 添加自定义头
        if (headers != null) {
            headers.forEach(spec::header);
            logger.debug("自定义头: {}", headers);
        }

        // 添加默认头
        defaultHeaders.forEach(spec::header);

        // 添加 Cookie
        if (cookies != null) {
            cookies.forEach(spec::cookie);
        }
        defaultCookies.forEach(spec::cookie);

        // 添加请求体
        if (body != null) {
            if (body instanceof String) {
                spec.body((String) body);
            } else if (body instanceof File) {
                spec.body((File) body);
            } else if (body instanceof Map) {
                spec.body(objectMapper.valueToTree(body).toString());
            } else {
                spec.body(body);
            }
            logger.debug("请求体: {}", body);
        }

        // 执行请求并记录时间
        long startTime = System.currentTimeMillis();
        Response response = null;

        try {
            switch (method.toUpperCase()) {
                case "GET":
                    response = spec.get(url);
                    break;
                case "POST":
                    response = spec.post(url);
                    break;
                case "PUT":
                    response = spec.put(url);
                    break;
                case "DELETE":
                    response = spec.delete(url);
                    break;
                case "PATCH":
                    response = spec.patch(url);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的 HTTP 方法: " + method);
            }

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            // 记录响应信息
            logResponse(response, responseTime);

            return response;

        } catch (Exception e) {
            logger.error("❌ API 请求失败: {} {}", method, url, e);
            throw new RuntimeException("API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 记录响应信息
     */
    private void logResponse(Response response, long responseTime) {
        int statusCode = response.getStatusCode();
        String statusLine = response.getStatusLine();
        String contentType = response.getContentType();
        String responseBody = response.getBody().asString();

        logger.info("📥 响应状态: {} ({}) - {}ms", statusCode, statusLine, responseTime);
        logger.debug("响应类型: {}", contentType);

        // 根据状态码记录不同级别的日志
        if (statusCode >= 200 && statusCode < 300) {
            logger.debug("响应体: {}", formatResponseBody(responseBody));
        } else if (statusCode >= 400 && statusCode < 500) {
            logger.warn("客户端错误响应: {}", formatResponseBody(responseBody));
        } else if (statusCode >= 500) {
            logger.error("服务器错误响应: {}", formatResponseBody(responseBody));
        }

        // 记录响应头
        Headers headers = response.getHeaders();
        if (headers.exist()) {
            logger.debug("响应头:");
            for (Header header : headers) {
                logger.debug("  {}: {}", header.getName(), header.getValue());
            }
        }
    }

    /**
     * 格式化响应体（JSON 美化）
     */
    private String formatResponseBody(String body) {
        if (body == null || body.isEmpty()) {
            return "<空响应>";
        }

        try {
            // 尝试美化成 JSON
            Object json = objectMapper.readValue(body, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            // 如果不是 JSON，返回原始内容（截断过长的内容）
            return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
        }
    }

    /**
     * 构建完整 URL
     */
    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return baseUrl + endpoint;
    }

    /**
     * 设置基础 URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        RestAssured.baseURI = baseUrl;
        logger.info("设置基础 URL: {}", baseUrl);
    }

    /**
     * 设置默认请求头
     */
    public void setDefaultHeader(String name, String value) {
        defaultHeaders.put(name, value);
        logger.debug("设置默认头: {} = {}", name, value);
    }

    /**
     * 设置认证令牌
     */
    public void setAuthToken(String token) {
        setDefaultHeader("Authorization", "Bearer " + token);
    }

    /**
     * 设置 Basic 认证
     */
    public void setBasicAuth(String username, String password) {
        requestSpec.auth().basic(username, password);
    }

    /**
     * 设置 OAuth2 认证
     */
    public void setOAuth2(String token) {
        setDefaultHeader("Authorization", "Bearer " + token);
    }

    /**
     * 上传文件
     */
    public Response uploadFile(String endpoint, String filePath, String paramName) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        logger.info("📎 上传文件: {} -> {}", filePath, endpoint);
        return given()
                .spec(requestSpec)
                .multiPart(paramName, file)
                .post(buildUrl(endpoint));
    }

    /**
     * 验证响应状态码
     */
    public void assertStatusCode(Response response, int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        if (actualStatusCode != expectedStatusCode) {
            String message = String.format("状态码验证失败: 期望 %d, 实际 %d\n响应体: %s",
                    expectedStatusCode, actualStatusCode, response.getBody().asString());
            throw new AssertionError(message);
        }
        logger.info("✅ 状态码验证通过: {}", expectedStatusCode);
    }

    /**
     * 验证响应体包含特定字段
     */
    public void assertResponseContains(Response response, String jsonPath, Object expectedValue) {
        Object actualValue = response.jsonPath().get(jsonPath);
        if (!expectedValue.equals(actualValue)) {
            String message = String.format("响应体验证失败: %s\n期望: %s\n实际: %s",
                    jsonPath, expectedValue, actualValue);
            throw new AssertionError(message);
        }
        logger.info("✅ 响应体验证通过: {} = {}", jsonPath, expectedValue);
    }

    /**
     * 验证响应时间
     */
    public void assertResponseTime(Response response, long maxTimeInMillis) {
        long responseTime = response.getTime();
        if (responseTime > maxTimeInMillis) {
            String message = String.format("响应时间验证失败: 最大 %dms, 实际 %dms",
                    maxTimeInMillis, responseTime);
            throw new AssertionError(message);
        }
        logger.info("✅ 响应时间验证通过: {}ms ≤ {}ms", responseTime, maxTimeInMillis);
    }

    /**
     * 提取响应中的值
     */
    public <T> T extractValue(Response response, String jsonPath, Class<T> type) {
        T value = response.jsonPath().getObject(jsonPath, type);
        logger.debug("从响应中提取 {} = {}", jsonPath, value);
        return value;
    }

    /**
     * 提取响应头
     */
    public String extractHeader(Response response, String headerName) {
        String value = response.getHeader(headerName);
        logger.debug("从响应头提取 {} = {}", headerName, value);
        return value;
    }

    /**
     * 提取 Cookie
     */
    public String extractCookie(Response response, String cookieName) {
        String value = response.getCookie(cookieName);
        logger.debug("从 Cookie 提取 {} = {}", cookieName, value);
        return value;
    }

    /**
     * 保存响应到文件
     */
    public void saveResponseToFile(Response response, String filePath) {
        try {
            String responseBody = response.getBody().asString();
            FileUtils.writeToFile(filePath, responseBody);
            logger.info("响应已保存到: {}", filePath);
        } catch (Exception e) {
            logger.error("保存响应到文件失败", e);
        }
    }

    /**
     * 健康检查
     */
    public boolean healthCheck(String endpoint) {
        try {
            Response response = get(endpoint);
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            logger.error("API 健康检查失败", e);
            return false;
        }
    }

    /**
     * 重置配置
     */
    public void reset() {
        defaultHeaders.clear();
        defaultCookies.clear();
        initialize();
        logger.info("API 配置已重置");
    }

    /**
     * 关闭资源
     */
    public void close() {
        // 目前没有需要关闭的资源，预留方法
        logger.info("API 工具已关闭");
    }
}