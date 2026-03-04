package com.company.ecommerce.ui;

import com.company.ecommerce.base.BaseUITest;
import com.company.ecommerce.ui.pages.ContractListPage;
import com.company.ecommerce.ui.pages.CreateContractPage;
import com.company.ecommerce.ui.pages.SendContractPage;
import com.company.ecommerce.utils.testdata.TestDataProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CreateContractTests extends BaseUITest {

    private CreateContractPage createContractPage;
    private SendContractPage sendContractPage;
    private ContractListPage contractListPage;

    private String cookieStr = "jenkins-timestamper-offset=-28800000; Idea-2a60ec10=e905b4f6-4b1c-490e-a68e-7ce6c63f890b; _ga=GA1.1.644306265.1771168578; _ga_FVWC4GKEYS=GS2.1.s1771168578$o1$g1$t1771168599$j39$l0$h0; itrustoken=VmPghXF5fyyQWrzEr7j";
    @BeforeMethod
    public void initPages() {
//        createContractPage = new CreateContractPage(driver,cookieStr);
        createContractPage = new CreateContractPage(driver);

    }



    @Test(dataProvider = "uicaseData", dataProviderClass = TestDataProvider.class)
    public void testCreateContract(String inputMapString, String expectedMapString ) throws JsonProcessingException {
//        System.out.println(inputMapString);
        // 转换为 Map<String, String>
        Map<String, String> inputMap = new ObjectMapper().readValue(inputMapString, new TypeReference<LinkedHashMap<String, String>>() {});
        createContractPage.navigateTo();
        sendContractPage = createContractPage.createContract(inputMap);

        sendContractPage.waitForPageLoad();
        contractListPage = sendContractPage.sendContract();

        Map<String, String> expectedMap = new ObjectMapper().readValue(expectedMapString, new TypeReference<Map<String, String>>() {});
        contractListPage.searchByMap(expectedMap);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        contractListPage.assertByMap(expectedMap);
    }

    @Test
    public void test(){
        sendContractPage = new SendContractPage(driver);
        sendContractPage.navigateTo();
        sendContractPage.sendContract();
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
