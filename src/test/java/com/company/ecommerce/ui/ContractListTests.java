package com.company.ecommerce.ui;

import com.company.ecommerce.base.BaseUITest;
import com.company.ecommerce.ui.pages.ContractListPage;
import com.company.ecommerce.utils.testdata.TestDataProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static java.lang.Thread.sleep;

public class ContractListTests extends BaseUITest {

    private ContractListPage contractListPage;
    @BeforeMethod
    public void initPages() {
        contractListPage = new ContractListPage(driver);
    }



    @Test(dataProvider = "uicaseData", dataProviderClass = TestDataProvider.class)
    public void testSearch(String searchMapString,String expectedText ) throws JsonProcessingException {
        // 转换为 Map<String, String>
        Map<String, String> searchMap = new ObjectMapper().readValue(searchMapString, new TypeReference<Map<String, String>>() {});
        contractListPage.navigateTo();
        contractListPage.searchByMap(searchMap);
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
