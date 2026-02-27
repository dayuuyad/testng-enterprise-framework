// src/test/java/com/company/ecommerce/base/BaseUITest.java
package com.company.ecommerce.base;

import com.company.ecommerce.config.ConfigManager;
import com.company.ecommerce.utils.WebDriverManagerUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;

public class BaseUITest extends BaseTest{

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @BeforeSuite
    public void setupSuite() {
//        RestAssured.baseURI = ConfigManager.getApiBaseUrl();
//        RestAssured.basePath = ConfigManager.getApiBasePath();
        baseUrl = ConfigManager.getInstance().getWebBaseUrl();
    }

    @BeforeClass
    public void setupClass() {
//        driver = WebDriverManagerUtil.getDriver();
    }

    @BeforeMethod
    public void setup(Method method) {
        // 获取测试方法名用于日志记录
        String testName = method.getName();
        System.out.println("=== Starting test: " + testName + " ===");

        // 初始化WebDriver
        driver = WebDriverManagerUtil.getDriver();

        // 设置等待时间
//        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 最大化窗口
        driver.manage().window().maximize();

        // 设置隐式等待
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // 设置页面加载超时
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        String testName = result.getName();

        // 如果测试失败，截图
        if (result.getStatus() == ITestResult.FAILURE) {
            takeScreenshot(testName);
            System.out.println("Test FAILED: " + testName);
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            System.out.println("Test PASSED: " + testName);
        } else {
            System.out.println("Test SKIPPED: " + testName);
        }

        // 关闭浏览器
        if (driver != null) {
            WebDriverManagerUtil.quitDriver();
//            driver.quit();
        }
        System.out.println("=== Finished test: " + testName + " ===\n");
    }

    @AfterSuite
    public void cleanupSuite() {
//        WebDriverManager.quitAllDrivers();
    }

    /**
     * 截图方法
     * @param testName 测试名称
     */
    protected void takeScreenshot(String testName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);

            // 创建截图目录
            String screenshotDir = "test-output/screenshots/";
            File directory = new File(screenshotDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 保存截图
            String destination = screenshotDir + testName + "_" + System.currentTimeMillis() + ".png";
            FileUtils.copyFile(source, new File(destination));
            System.out.println("Screenshot saved: " + destination);
        } catch (IOException e) {
            System.out.println("Failed to take screenshot: " + e.getMessage());
        }
    }


}