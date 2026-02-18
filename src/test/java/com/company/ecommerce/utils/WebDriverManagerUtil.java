package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
import com.company.ecommerce.reporters.AllureManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class WebDriverManagerUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverManagerUtil.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            createDriver();
        }
        return driverThreadLocal.get();
    }

    private static void createDriver() {
        String browser = ConfigManager.getInstance().getBrowserName().toLowerCase();
        boolean headless = ConfigManager.getInstance().isHeadless();

        logger.info("启动浏览器: {}, headless: {}", browser, headless);

        WebDriver driver = null;

        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--window-size=1920,1080");

                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }

                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;

            default:
                throw new IllegalArgumentException("不支持的浏览器: " + browser);
        }

        // 设置超时
        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.implicit.wait", 10))
        );
        driver.manage().window().maximize();

        driverThreadLocal.set(driver);
        logger.info("浏览器启动成功");
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                // 最后再截张图
                takeScreenshot("final_state");
                driver.quit();
                logger.info("浏览器已关闭");
            } catch (Exception e) {
                logger.error("关闭浏览器失败", e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    @Attachment(value = "{screenshotName}", type = "image/png")
    public static byte[] takeScreenshot(String screenshotName) {
        WebDriver driver = driverThreadLocal.get();
        if (driver instanceof TakesScreenshot) {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return new byte[0];
    }
}