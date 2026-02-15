// src/test/java/com/company/ecommerce/base/BaseAPITest.java
package com.company.ecommerce.base;

import com.company.ecommerce.config.ConfigManager;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class BaseAPITest extends BaseTest {

    protected RequestSpecification requestSpec;


    @BeforeClass(alwaysRun = true)
    public void apiSetup() {

        RestAssured.baseURI = ConfigManager.getApiBaseUrl();
        logger.info("-------------------RestAssured.baseURI: {}",RestAssured.baseURI);
//        RestAssured.basePath = ConfigManager.getApiBasePath();

//        requestSpec = given()
//            .header("Content-Type", "application/json")
//            .header("Authorization", "Bearer " + ConfigManager.getApiToken())
//            .filter(new RequestLoggingFilter())
//            .filter(new ResponseLoggingFilter());
    }

    protected RequestSpecification givenAuth(Object requestBody)  {

        ObjectMapper mapper = new ObjectMapper()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBodyToJson;
        try {
             requestBodyToJson = mapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
        // 将请求体包含在签名中
//        String requestBodyToJson = requestBody != null ? requestBody.toString() : "";
//        System.out.println(requestBodyToJson);
//        requestBodyToJson="{\"displayName\":\"巫超航\",\"idCardType\":\"0\",\"idCardNum\":\"130520198508093243\",\"phone\":\"15006713756\",\"authentication\":true,\"passwd\":\"Aa123456\"}";

        String signature = generateHmacSha1Signature(requestBodyToJson);

        requestSpec = given()
                .contentType(ContentType.JSON)  // 等同于 "application/json;charset=UTF-8"
                .header("appid", appId)
                .header("servicecode", serviceCode)
                .header("Content-Signature", "HMAC-SHA1 " + signature)
//                .header("Content-Signature", "HMAC-SHA1 " + "JWgwUNvQwdI/oyTiw4DeqYZVdtg=")
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        // 如果有请求体，添加到请求中
        if (requestBodyToJson != null) {
            requestSpec.body(requestBodyToJson);
            // 可选：添加请求体的SHA1哈希值
//            spec.header("X-Content-SHA1", calculateSha1Hash(bodyString));
        }

        return requestSpec;
    }

    protected RequestSpecification givenAuth() {
        String timestamp = getCurrentTimestamp();
        String nonce = generateNonce();
        String signature = generateSignature(timestamp, nonce);

        return given()
                .contentType(ContentType.JSON)  // 等同于 "application/json;charset=UTF-8"
                .header("appid", appId)
                .header("servicecode", serviceCode)
                .header("Content-Signature", "HMAC-SHA1 " + signature)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
    }

    //    private String generateHmacSha1Signature(String timestamp, String nonce, String body) {
    private String generateHmacSha1Signature(String body) {
        try {
            // 1. 构建待签名字符串（根据实际API文档调整格式）
            StringBuilder stringToSign = new StringBuilder();
//            stringToSign.append(timestamp).append("\n");
//            stringToSign.append(nonce).append("\n");
//            stringToSign.append(APP_ID).append("\n");

            // 如果存在请求体，添加到签名中
            if (body != null && !body.isEmpty()) {
                stringToSign.append(body);
            }

            // 2. 初始化HMAC-SHA1
            Mac mac = Mac.getInstance(hmacSha1Algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(
                    (secretKey + serviceCode).getBytes(StandardCharsets.UTF_8),
                    hmacSha1Algorithm
            );
            mac.init(keySpec);
//            System.out.println("1111111111111111111111111111"+stringToSign.toString());
            // 3. 计算签名
            byte[] hmacBytes = mac.doFinal(
                    stringToSign.toString().getBytes(StandardCharsets.UTF_8)
            );

            // 4. Base64编码
            return Base64.getEncoder().encodeToString(hmacBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("HMAC-SHA1算法不可用", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("无效的密钥", e);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA1签名生成失败", e);
        }
    }

    private String generateSignature(String timestamp, String nonce) {
        try {
            String dataToSign = timestamp + nonce + appId;

            Mac mac = Mac.getInstance(hmacSha1Algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(
                    (BaseTest.secretKey + serviceCode).getBytes(StandardCharsets.UTF_8),
                    hmacSha1Algorithm
            );
            mac.init(secretKey);

            byte[] hmacBytes = mac.doFinal(
                    dataToSign.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hmacBytes);

        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA1签名生成失败", e);
        }
    }

    private String getCurrentTimestamp() {
        return String.valueOf(Instant.now().toEpochMilli());
    }

    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    protected RequestSpecification givenAuth1() {
        return requestSpec;
    }

}


//abstract class BaseAPITest2  {
//
//    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
//    private static final String SECRET_KEY = ConfigManager.getApiSecretKey();
//    private static final String API_KEY = ConfigManager.getApiAppId();
//
//    protected RequestSpecification givenAuth() {
//        String timestamp = getCurrentTimestamp();
//        String nonce = generateNonce();
//        String signature = generateSignature(timestamp, nonce);
//
//        return given()
//                .header("Content-Type", "application/json")
//                .header("Authorization", "HMAC-SHA1 " + signature)
//                .header("X-API-Key", API_KEY)
//                .header("X-Timestamp", timestamp)
//                .header("X-Nonce", nonce)
//                .filter(new RequestLoggingFilter())
//                .filter(new ResponseLoggingFilter());
//    }
//
//    private String generateSignature(String timestamp, String nonce) {
//        try {
//            String dataToSign = timestamp + nonce + API_KEY;
//
//            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
//            SecretKeySpec secretKey = new SecretKeySpec(
//                    SECRET_KEY.getBytes(StandardCharsets.UTF_8),
//                    HMAC_SHA1_ALGORITHM
//            );
//            mac.init(secretKey);
//
//            byte[] hmacBytes = mac.doFinal(
//                    dataToSign.getBytes(StandardCharsets.UTF_8)
//            );
//
//            return Base64.getEncoder().encodeToString(hmacBytes);
//
//        } catch (Exception e) {
//            throw new RuntimeException("HMAC-SHA1签名生成失败", e);
//        }
//    }
//
//    private String getCurrentTimestamp() {
//        return String.valueOf(Instant.now().toEpochMilli());
//    }
//
//    private String generateNonce() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }
//}