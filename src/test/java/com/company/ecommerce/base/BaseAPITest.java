// src/test/java/com/company/ecommerce/base/BaseAPITest.java
package com.company.ecommerce.base;

import com.company.ecommerce.utils.ConfigManagerbak;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import static io.restassured.RestAssured.given;

public abstract class BaseAPITest extends BaseTest {
    
    protected RequestSpecification requestSpec;
    
    @BeforeClass
    public void apiSetup() {
        RestAssured.baseURI = ConfigManagerbak.getApiBaseUrl();
        RestAssured.basePath = ConfigManagerbak.getApiBasePath();
        
        requestSpec = given()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + ConfigManagerbak.getApiToken())
            .filter(new RequestLoggingFilter())
            .filter(new ResponseLoggingFilter());
    }
    
    protected RequestSpecification givenAuth() {
        return requestSpec;
    }
}