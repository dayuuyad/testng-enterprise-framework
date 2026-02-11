// src/test/java/com/company/ecommerce/utils/TestDataProvider.java
package com.company.ecommerce.utils;

import com.company.ecommerce.models.Product;
import com.company.ecommerce.models.User;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.Iterator;

public class TestDataProvider {

    @DataProvider(name = "loginData")
    public static Object[][] getLoginData() {
        return new Object[][] {
                {"standard_user", "secret_sauce", true},
                {"locked_out_user", "secret_sauce", false},
                {"problem_user", "secret_sauce", true},
                {"performance_glitch_user", "secret_sauce", true}
        };
    }

    @DataProvider(name = "userData")
    public Iterator<Object[]> getUserData() throws IOException {
        return ExcelReader.readTestData("users.xlsx", "UserData");
    }

    @DataProvider(name = "productData")
    public Object[][] getProductData() {
        return JsonReader.readTestData("products.json", Product[].class);
    }

    public static User createTestUser() {
        return User.builder()
                .username("testuser_" + System.currentTimeMillis())
                .email("test" + System.currentTimeMillis() + "@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .build();
    }
}