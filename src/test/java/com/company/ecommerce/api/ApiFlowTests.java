package com.company.ecommerce.api;

import com.company.ecommerce.base.BaseAPITest;
import com.company.ecommerce.utils.ExcelReader;
import com.company.ecommerce.utils.ParameterResolver;
import com.company.ecommerce.utils.testdata.TestDataProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

public class ApiFlowTests  extends BaseAPITest {

    private Map<String, Object> context;
    private ParameterResolver parameterResolver;

    @BeforeClass
    public void setUp() {
        context = new HashMap<>();
        parameterResolver =new ParameterResolver(context);

    }

    @Test(dataProvider = "flowName", dataProviderClass = TestDataProvider.class)
    public void testApiFlow(String sheetName)  {
        //获取所有流程用例
        Iterator<Map<String, String>> mapIterator = ExcelReader.getUserDataAsMap("flow.xlsx", sheetName);
        while (mapIterator.hasNext()) {
            Map<String, String> map = mapIterator.next();
            //处理请求参数，上下文ID，程序生成ID
            JsonNode requestBody=parameterResolver.requestResolve(map.get("requestBody"));
            Response response = post(map.get("url"), requestBody);
            response.then()
                    .statusCode(200)
                    .body("status", equalTo(1))
            ;
            //获取ID放入上下文
            parameterResolver.responseSave(map.get("responseExtracts"),response);
        }
        context.clear();
    }
}
