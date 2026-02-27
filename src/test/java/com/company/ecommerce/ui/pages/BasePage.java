// src/test/java/com/company/ecommerce/ui/pages/BasePage.java
package com.company.ecommerce.ui.pages;

import com.company.ecommerce.config.ConfigManager;
import com.company.ecommerce.utils.WebDriverManagerUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class BasePage {

    protected WebDriver driver;
    protected String baseUrl;
    protected WebDriverWait wait;
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected String cookieStr;

    public BasePage(WebDriver driver) {
        this.driver = driver;
//        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.wait = WebDriverManagerUtil.getWait();
        this.cookieStr = ConfigManager.getInstance().getCookieStr();
        PageFactory.initElements(driver, this);
    }

    protected void click(WebElement element) {
        logger.info("点击元素: {}", element.getText());
        element.click();
    }

    protected void click(WebElement element,String elementName) {
        logger.info("点击元素: {}", elementName);
        element.click();
    }

    protected void waitToClick(WebElement element) {
        waitForElementToBeClickable(element);
//        logger.info("点击元素: {}", elementName);
        element.click();
    }

    protected void type(WebElement element, String text, String elementName) {
        logger.info("在 {} 输入: {}", elementName, text);
        element.clear();
        element.sendKeys(text);
    }

    protected boolean isDisplayed(WebElement element, String elementName) {
        boolean displayed = element.isDisplayed();
        logger.info("元素 {} 是否显示: {}", elementName, displayed);
        return displayed;
    }

    protected boolean isDisplayed(WebElement element) {
        return element.isDisplayed();
    }

    /**
     * 导航到相对路径
     * @param relativeUrl 相对URL
     */
    protected void navigateTo(String relativeUrl) {
//        baseUrl = System.getProperty("app.base.url", "http://localhost:8080");
        baseUrl = ConfigManager.getInstance().getWebBaseUrl();
        driver.get(baseUrl + relativeUrl);


        // 2. 分割并添加所有Cookie
        String[] cookies = cookieStr.split("; ");
        for (String cookie : cookies) {
            String[] parts = cookie.split("=", 2); // 限制分割为2部分，防止value中包含=
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = parts[1].trim();

                // 创建Cookie对象并添加
                Cookie seleniumCookie = new Cookie(name, value);
                driver.manage().addCookie(seleniumCookie);

//                System.out.println("添加Cookie: " + name + " = " + value);
            }
        }

        driver.navigate().refresh();

    }

    /**
     * 等待元素可见
     * @param element 元素
     */
    protected void waitForElementToBeVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * 等待元素可点击
     * @param element 元素
     */
    protected void waitForElementToBeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * 清空输入框并输入文本
     * @param element 输入框元素
     * @param text 要输入的文本
     */
    protected void clearAndType(WebElement element, String text) {
        waitForElementToBeVisible(element);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * 获取当前URL
     * @return 当前URL
     */
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * 等待页面加载完成
     */
    protected void waitForPageLoad() {
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
    }

    /**
     * 执行JavaScript脚本
     * @param script JavaScript脚本
     * @param args 参数
     * @return 执行结果
     */
    protected Object executeJavaScript(String script, Object... args) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return js.executeScript(script, args);
    }

    /**
     * 获取页面标题
     * @return 页面标题
     */
    protected String getPageTitle() {
        return driver.getTitle();
    }


    // 获取弹窗文本
    public String getAlertText() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement  alert = wait.until( ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[role='alert']")
        ));
        return alert.findElement(By.cssSelector(".el-message__content")).getText();
    }

}