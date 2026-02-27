// src/test/java/com/company/ecommerce/utils/TestDataProvider.java
package com.company.ecommerce.utils.testdata;

import com.company.ecommerce.models.Product;
import com.company.ecommerce.models.User;
import com.company.ecommerce.utils.ExcelReader;
import com.company.ecommerce.utils.JsonReader;
import com.company.ecommerce.utils.testdata.datacreate.ChineseName;
import com.company.ecommerce.utils.testdata.datacreate.IdCardNum;
import com.company.ecommerce.utils.testdata.datacreate.Mobile;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

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

    @DataProvider(name = "flowName")
    public Iterator<String> getExcelSheetname() {
        return ExcelReader.getSheetNames("flow.xlsx");
    }

    @DataProvider(name = "userData")
    public Iterator<Object[]> getUserData() throws IOException {
        return ExcelReader.readTestData("users.xlsx", "UserData");
    }

    @DataProvider(name = "caseDataMap")
    public Iterator<Map<String, String>> getCaseDataMap(Method method) throws IOException {
        return ExcelReader.getUserDataAsMap("single.xlsx", method.getName());
    }

    @DataProvider(name = "uicaseData")
    public  Iterator<Object[]> getUICaseDataMap(Method method) throws IOException {
        return ExcelReader.readTestData("ui/pagecases.xlsx", method.getName());
    }

    @DataProvider(name = "productData")
    public Object[][] getProductData() {
        return JsonReader.readTestData("products.json", Product[].class);
    }

    public static User createTestUser() {
        return User.builder()
                .authentication(true)
                .displayName(new ChineseName().toString())
                .idCardType("0")
                .idCardNum(new IdCardNum().toString())
                .passwd("Aa123456")
                .phone(new Mobile().toString())
                .build();
    }
}
