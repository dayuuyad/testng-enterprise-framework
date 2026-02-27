// src/test/java/com/company/ecommerce/ui/pages/LoginPage.java
package com.company.ecommerce.ui.pages;

import com.company.ecommerce.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    // 页面元素
    @FindBy(css = "input[placeholder='请输入您的手机号/邮箱/账号名']")
    private WebElement usernameField;

    @FindBy(css = "input[placeholder='请输入密码']")
    private WebElement passwordField;

    @FindBy(css = "input[placeholder='请输入验证码']")
    private WebElement verificationCodeField;

    @FindBy(css = "button[type='button']")
    private WebElement loginButton;


    @FindBy(id = "username")
    private WebElement usernameField1;

    @FindBy(id = "password")
    private WebElement passwordField1;

    @FindBy(id = "login-button")
    private WebElement loginButton1;

    @FindBy(className = "error-message")
    private WebElement errorMessage;

    @FindBy(linkText = "Forgot Password?")
    private WebElement forgotPasswordLink;

    // 构造器
    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // 页面操作方法
    public void navigateTo() {
        driver.get(ConfigManager.getInstance().getWebBaseUrl() );
        waitForPageLoad();
    }

    public void login(String username, String password) {
        type(usernameField, username, "用户名输入框");
        type(passwordField, password, "密码输入框");
        type(verificationCodeField, "000000", "验证码");

        click(loginButton, "登录按钮");
        System.out.println(getAlertText());
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessage, "错误消息");
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink, "忘记密码链接");
    }

    // 业务流方法
    public HomePage loginWithValidCredentials(String username, String password) {
        login(username, password);
        return new HomePage(driver);
    }
}