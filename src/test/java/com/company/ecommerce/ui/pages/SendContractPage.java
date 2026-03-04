package com.company.ecommerce.ui.pages;

import cn.hutool.core.map.MapUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

// page_url = http://localhost:8888/#/subview/contractweb/contractManage/sendtemplate/sendContract?id=243056470692724749&successCallbackRouteName
public class SendContractPage extends BasePage {
    // 印章控件
    @FindBy(xpath = "//div[@class='item'][.//img[contains(@src, 'jy')]]")
    private WebElement divSeal;
    // 合同文档
//    @FindBy(id = "canvas_render0")
    @FindBy(xpath = "//div[@class=\"canvas_parent\"]")
    private WebElement contractDocument;

    // 发送按钮
    @FindBy(css = "button[class*='el-button--primary']")
    public WebElement buttonSend;

    public void navigateTo() {
        navigateTo("#/subview/contractweb/contractManage/sendtemplate/sendContract?id=243059216250568733&successCallbackRouteName");
//        waitForPageLoad();
    }

    public SendContractPage(WebDriver driver) {
        super(driver);
    }

    public ContractListPage sendContract () {
        waitElementLocatedInvisibility();
        rollToWebElement(divSeal);
        waitForElementToBePresence(By.id("canvas_render0"));
//        waitForElementToBeVisible(contractDocument);
        dragWebElement(divSeal, contractDocument);
        waitToClick(buttonSend);
        return new ContractListPage(driver);

    }
}