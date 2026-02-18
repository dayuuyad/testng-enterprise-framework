// src/test/java/com/company/ecommerce/api/UserAPITests.java
package com.company.ecommerce.api;

import com.company.ecommerce.base.BaseAPITest;
import com.company.ecommerce.models.User;
import com.company.ecommerce.utils.testdata.TestDataProvider;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static com.company.ecommerce.constants.ContractEndpoint.CREATE_USER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserAPITests extends BaseAPITest {

    private User testUser;
    private String createdUserId;

    @Test(
            groups = {"api", "smoke", "user"},
            description = "创建新用户API测试"
    )
    public void testCreateUser() {
        // Given
        testUser = TestDataProvider.createTestUser();
//        logger.info(testUser.toString());
        // When
//        Response response = givenAuth(testUser)
//                .post(CREATE_USER);

        Response response = post(CREATE_USER,testUser);

        // Then
        response.then()
                .statusCode(200)
                .body("status", equalTo(1))
//                .body("id", notNullValue())
//                .body("username", equalTo(testUser.getDisplayName()))
//                .body("email", equalTo(testUser.getEmail()))
        ;

        createdUserId = response.jsonPath().getString("data.userId");
    }

    @Test(
            groups = {"api", "regression", "user"},
            dependsOnMethods = "testCreateUser",
            description = "获取用户详情API测试"
    )
    public void testGetUserById() {
        // Given - createdUserId from previous test

        // When
        Response response = givenAuth()
                .get("/users/{id}", createdUserId);

        // Then
        response.then()
                .statusCode(200)
                .body("id", equalTo(createdUserId))
                .body("username", equalTo(testUser.getDisplayName()));
    }

    @Test(
            groups = {"api", "user", "search"},
            description = "搜索用户API测试"
    )
    public void testSearchUsers() {
        // When
        Response response = givenAuth()
                .queryParam("username", "test")
                .get("/users/search");

        // Then
        response.then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].username", containsString("test"));
    }

    @Test(
            groups = {"api", "security", "user"},
            description = "验证未授权访问应该被拒绝"
    )
    public void testUnauthorizedAccess() {
        // When - 不使用认证
        Response response = given()
                .get("/users");

        // Then
        response.then()
                .statusCode(401);
    }
}