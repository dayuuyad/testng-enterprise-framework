package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一的WebDriver管理器
 * 综合了两个版本的优点：完整功能 + Allure集成
 */
public class WebDriverManagerUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverManagerUtil.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();

    private WebDriverManagerUtil() {
        // 工具类，防止实例化
    }

    // ==================== 初始化方法 ====================

    public static WebDriver getDriver() {
        WebDriver webDriver=driverThreadLocal.get();
        if (driverThreadLocal.get() == null) {
            initializeDriver();
        }
        return driverThreadLocal.get();
    }

    private static void initializeDriver() {
        String browserName = ConfigManager.getInstance().getBrowserName().toLowerCase();
        boolean headless = ConfigManager.getInstance().isHeadless();

        logger.info("初始化浏览器: {}, headless: {}", browserName, headless);

        WebDriver driver;

        switch (browserName) {
            case "chrome":
                driver = createChromeDriver(headless);
                break;
            case "firefox":
                driver = createFirefoxDriver(headless);
                break;
            case "edge":
                driver = createEdgeDriver(headless);
                break;
            case "safari":
                driver = createSafariDriver();
                break;
            default:
                throw new IllegalArgumentException("不支持的浏览器: " + browserName);
        }

        configureDriver(driver);
        driverThreadLocal.set(driver);
        logger.info("浏览器初始化成功");
    }

    public static WebDriverWait getWait() {
        if (waitThreadLocal.get() == null) {
            waitThreadLocal.set(new WebDriverWait(getDriver(),
                    Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.implicit.wait", 10))));
        }
        return waitThreadLocal.get();
    }

    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Headless模式
        if (headless) {
            options.addArguments("--headless=new");
        }

        // 常用参数
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--disable-infobars",
                "--disable-notifications",
                "--disable-popup-blocking",
                "--disable-web-security",
                "--allow-running-insecure-content",
                "--window-size=1920,1080"
        );

        // 性能优化
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-features=VizDisplayCompositor");

        // 实验性选项
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // 移动端模拟
        if (ConfigManager.getInstance().getBooleanProperty("mobile.emulation", false)) {
            Map<String, Object> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", "iPhone 12");
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
        }

        // 偏好设置
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("download.default_directory", System.getProperty("user.dir") + "/downloads");
        options.setExperimentalOption("prefs", prefs);

        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--width=1920",
                "--height=1080"
        );

        // Firefox偏好设置
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("geo.enabled", false);
        options.addPreference("media.navigator.enabled", false);

        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
//        WebDriverManager.edgedriver().setup();
        System.setProperty("webdriver.edge.driver", "driver/msedgedriver.exe");  // Windows

        EdgeOptions options = new EdgeOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--inprivate",
                "--window-size=1920,1080"
        );

        return new EdgeDriver(options);
    }

    private static WebDriver createSafariDriver() {
        // Safari不支持headless模式
        return new SafariDriver();
    }

    private static void configureDriver(WebDriver driver) {
        // 设置超时
        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.implicit.wait", 10)));

        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.page.load.timeout", 30)));

        driver.manage().timeouts().scriptTimeout(
                Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.script.timeout", 30)));

        // 最大化窗口
        driver.manage().window().maximize();
    }

    // ==================== 关闭方法 ====================

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                // 最后截图
//                takeScreenshot("final_state");
                driver.quit();
                logger.info("浏览器已关闭");
            } catch (Exception e) {
                logger.error("关闭浏览器失败: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
                waitThreadLocal.remove();
            }
        }
    }

    // ==================== Allure截图 ====================

    @Attachment(value = "截图: {screenshotName}", type = "image/png")
    public static byte[] takeScreenshot(String screenshotName) {
        WebDriver driver = driverThreadLocal.get();
        if (driver instanceof TakesScreenshot) {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return new byte[0];
    }

    // ==================== 窗口操作 ====================

    public static void maximizeWindow() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.manage().window().maximize();
        }
    }

    public static void minimizeWindow() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.manage().window().minimize();
        }
    }

    public static void setWindowSize(int width, int height) {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.manage().window().setSize(new Dimension(width, height));
        }
    }

    // ==================== 导航操作 ====================

    public static String getCurrentUrl() {
        WebDriver driver = driverThreadLocal.get();
        return driver != null ? driver.getCurrentUrl() : "";
    }

    public static String getPageTitle() {
        WebDriver driver = driverThreadLocal.get();
        return driver != null ? driver.getTitle() : "";
    }

    public static void navigateTo(String url) {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.get(url);
        }
    }

    public static void navigateBack() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.navigate().back();
        }
    }

    public static void navigateForward() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.navigate().forward();
        }
    }

    public static void refreshPage() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.navigate().refresh();
        }
    }

    // ==================== 窗口切换 ====================

    public static void switchToNewWindow() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            String originalWindow = driver.getWindowHandle();
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
        }
    }

    public static void switchToMainWindow() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            String mainWindow = driver.getWindowHandles().iterator().next();
            driver.switchTo().window(mainWindow);
        }
    }

    public static void closeCurrentWindow() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.close();
            switchToMainWindow();
        }
    }

    // ==================== 弹窗操作 ====================

    public static void acceptAlert() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.switchTo().alert().accept();
        }
    }

    public static void dismissAlert() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.switchTo().alert().dismiss();
        }
    }

    public static String getAlertText() {
        WebDriver driver = driverThreadLocal.get();
        return driver != null ? driver.switchTo().alert().getText() : "";
    }

    // ==================== JavaScript操作 ====================

    public static void waitForPageLoad() {
        getWait().until(d -> {
            String readyState = ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").toString();
            return readyState.equals("complete");
        });
    }

    public static void executeJavaScript(String script, Object... args) {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            ((JavascriptExecutor) driver).executeScript(script, args);
        }
    }

    public static void scrollToElement(WebElement element) {
        executeJavaScript("arguments[0].scrollIntoView(true);", element);
    }

    public static void scrollToBottom() {
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public static void scrollToTop() {
        executeJavaScript("window.scrollTo(0, 0)");
    }

    // ==================== 清理操作 ====================

    public static void clearCookies() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.manage().deleteAllCookies();
        }
    }

    public static void clearLocalStorage() {
        executeJavaScript("window.localStorage.clear();");
    }

    public static void clearSessionStorage() {
        executeJavaScript("window.sessionStorage.clear();");
    }
}