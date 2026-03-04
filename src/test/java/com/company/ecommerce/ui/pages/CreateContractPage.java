package com.company.ecommerce.ui.pages;

import cn.hutool.core.map.MapUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.company.ecommerce.utils.FileUtils;

// page_url = http://localhost:8888/#/subview/contractweb/contractManage/sendtemplate/createContract
public class CreateContractPage extends BasePage {

    // 业务类型
    @FindBy(css = "input[readonly='readonly']")
    private WebElement businesstype;

    // 业务类型下拉列表
    @FindBy(xpath = "//ul[@class=\"el-scrollbar__view el-select-dropdown__list\"]/li")
    private List<WebElement> businesstypeDropdownList;

    @FindBy(css = "input[placeholder=\"请输入合同标题\"]")
    private WebElement contractTitle;

    // 过期时间
    @FindBy(css = "input[aria-valuemin='1']")
    private WebElement expiryTime;

    @FindBy(css = "input[placeholder='请选择生效日期']")
    private WebElement EffectiveDate;

    @FindBy(xpath = "//button/span[text()=' 添加文件 ']")
    private WebElement addDocument;

    // 文件列表
    @FindBy(css = "ul[class$='fileList']")
    private WebElement fileList;

    @FindBy(xpath = "//div[normalize-space(text())='添加内部员工']")
    private WebElement addInternalEmployees;

    // 员工列表
    @FindBy(xpath = "//div[@role=\"treeitem\"]")
    private List<WebElement> selectInternalEmployees;

    // 选择员工保存按钮
    @FindBy(xpath = "//button/span[text()='保存']/..")
    private WebElement employeesSelectSaveButton;


    // 下一步
    @FindBy(css = "div[class='details_topbar_content_right'] button[class*='el-button--primary']")
    private WebElement buttonNextStep;

    // 输入框列表
    private Map<String, WebElement> inputElementMaps;




    public CreateContractPage(WebDriver driver) {
        super(driver);
        inputElementMaps = MapUtil.<String, WebElement>builder()
                .put("合同标题",contractTitle)
                .put("过期时间",expiryTime)
                .put("生效日期",EffectiveDate)
                .build();
    }

    public CreateContractPage(WebDriver driver, String cookieStr) {
        super(driver, cookieStr);
        inputElementMaps = MapUtil.<String, WebElement>builder()
                .put("合同标题",contractTitle)
                .put("过期时间",expiryTime)
                .put("生效日期",EffectiveDate)
                .build();
    }

    public void navigateTo() {
        navigateTo("#/subview/contractweb/contractManage/sendtemplate/createContract");
//        waitForPageLoad();
    }

    public SendContractPage createContract(Map<String, String> map) {
        map.forEach((key, value) -> {
                    switch (key) {
                        case "业务类型":
                            waitToClick(businesstype);
                            waitForElementToBeVisible(businesstypeDropdownList.get(0));
                            for (int i = 0; i < businesstypeDropdownList.size(); i++) {
//                                System.out.println("businesstypeDropdownList"+businesstypeDropdownList.get(i).getText());
//                                System.out.println(value);
                                if (businesstypeDropdownList.get(i).getText().equals(value)) {
//                                    System.out.println("111111111111");
                                    businesstypeDropdownList.get(i).click();
                                    break;
                                }
                            }
                            break;
                        case "添加文件":
                            rollToWebElement(addDocument);
                            waitToClick(addDocument);
                            FileUtils.uploadFileWithRobot(value);

//                    WebElement NOButton = webElement.findElement(By.xpath("./td[1]/div/div/span[text()='NO.']"));
//                    waitToClick(NOButton);
////                        String NO = driver.findElement(By.id(NOButton.getAttribute("aria-describedby"))).getText();
//
////                        String NO = waitForElementToBePresence(By.id(NOButton.getAttribute("aria-describedby"))).getText();
//                    String NO = waitForElementToBeVisible(driver.findElement(By.id(NOButton.getAttribute("aria-describedby")))).getText();
//
//                    System.out.println(NO);
//                    Assert.assertEquals(NO, value);

                            break;
                        case "添加内部员工":
                            rollToWebElement(addInternalEmployees);
                            waitToClick(addInternalEmployees);
                            waitForElementToBeVisible(selectInternalEmployees.get(0));
                            for (int i = 0; i < selectInternalEmployees.size(); i++) {
                                if (selectInternalEmployees.get(i).getText().equals(value)) {
                                    selectInternalEmployees.get(i).click();
                                    employeesSelectSaveButton.click();
                                    break;
                                }
                            }
                            break;
                        default:
                            type(inputElementMaps.get(key),value,key);
                            break;
                    }
                }
        );
        waitToClick(buttonNextStep);
        return new SendContractPage(driver);
    }
}