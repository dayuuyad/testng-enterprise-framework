package com.company.ecommerce.ui.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

// page_url = http://localhost:8888/#/subview/contractweb/contractManage/sendtemplate/sign?id=243070008429641970
public class SignContractPage extends BasePage {
    // 印章控件
    @FindBy(xpath = "//div/span[text()='印章位置']/..")
    private WebElement sealControl;

    // 印章列表
    @FindBy(xpath = "//div[@class='flex-sign-info flex-sign-info-seal']/div")
    private List<WebElement> sealList;

    // 选择印章确定按钮
    @FindBy(xpath = "/html/body/div[3]/div/div[3]/span/button")
    private WebElement confirmButton;

    // 立即签署
    @FindBy(xpath = "//button/span[normalize-space(text())='立即签署']/..")
    private WebElement signNow;

    // 确认勾选
    @FindBy(xpath = "//span[@class='el-checkbox__inner']")
    private List<WebElement> checkBoxes;

    // 密码框
    @FindBy(xpath = "//input[@type='password']")
    private WebElement passwordInput;

    // 签署提交
    @FindBy(xpath = "//button/span[normalize-space(text())='签署']/..")
    private WebElement signButton;

    public SignContractPage(WebDriver driver) {
        super(driver);
    }


    public ContractListPage sign() {
        waitForLoadingComplete();
        waitToClick(sealControl);
        sealList.get(0).click();
        confirmButton.click();

//        for (WebElement seal : sealList) {
//            seal.click();
//            confirmButton.click();
//            break;
//        }
        signNow.click();
        waitForLoadingComplete();
        for (WebElement checkBox : checkBoxes) {
            waitToClick(checkBox);
//            checkBox.click();
        }
        type(passwordInput,"1q","签署密码");
        signButton.click();
        return new ContractListPage(driver);
    }
}