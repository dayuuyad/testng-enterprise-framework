package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
//import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
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

public class WebDriverManager {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverManager.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> wait = new ThreadLocal<>();

    private WebDriverManager() {
        // Utility class, prevent instantiation
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            initializeDriver();
        }
        return driver.get();
    }

    public static WebDriverWait getWait() {
        if (wait.get() == null) {
            wait.set(new WebDriverWait(getDriver(),
                    Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.implicit.wait", 10))));
        }
        return wait.get();
    }

    private static void initializeDriver() {
        String browserName = ConfigManager.getInstance().getBrowserName().toLowerCase();
        boolean headless = ConfigManager.getInstance().isHeadless();

        logger.info("Initializing {} browser (headless: {})", browserName, headless);

        switch (browserName) {
            case "chrome":
                setupChromeDriver(headless);
                break;
            case "firefox":
                setupFirefoxDriver(headless);
                break;
            case "edge":
                setupEdgeDriver(headless);
                break;
            case "safari":
                setupSafariDriver();
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }

        // Configure driver settings
        configureDriver();
    }

    private static void setupChromeDriver(boolean headless) {
        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Headless mode
        if (headless) {
            options.addArguments("--headless=new");
        }

        // Common arguments
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

        // Performance optimizations
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-features=VizDisplayCompositor");

        // Experimental options
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // Mobile emulation (optional)
        if (ConfigManager.getInstance().getBooleanProperty("mobile.emulation", false)) {
            Map<String, Object> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", "iPhone 12");
            options.setExperimentalOption("mobileEmulation", mobileEmulation);
        }

        // Preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("download.default_directory", System.getProperty("user.dir") + "/downloads");
        options.setExperimentalOption("prefs", prefs);

        driver.set(new ChromeDriver(options));
    }

    private static void setupFirefoxDriver(boolean headless) {
        io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();

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

        // Firefox preferences
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("geo.enabled", false);
        options.addPreference("media.navigator.enabled", false);

        driver.set(new FirefoxDriver(options));
    }

    private static void setupEdgeDriver(boolean headless) {
        io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();

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

        driver.set(new EdgeDriver(options));
    }

    private static void setupSafariDriver() {
        // Safari doesn't support headless mode
        driver.set(new SafariDriver());
    }

    private static void configureDriver() {
        WebDriver webDriver = driver.get();

        // Set timeouts
        webDriver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.implicit.wait", 10)));

        webDriver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.page.load.timeout", 30)));

        webDriver.manage().timeouts().scriptTimeout(
                Duration.ofSeconds(ConfigManager.getInstance().getIntProperty("browser.script.timeout", 30)));

        // Maximize window if not headless
        if (!ConfigManager.getInstance().isHeadless()) {
            maximizeWindow();
        }

        logger.info("Browser initialized successfully");
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                driver.get().quit();
                logger.info("Browser closed successfully");
            } catch (Exception e) {
                logger.warn("Error while closing browser: {}", e.getMessage());
            } finally {
                driver.remove();
                wait.remove();
            }
        }
    }

    public static void maximizeWindow() {
        if (driver.get() != null) {
            driver.get().manage().window().maximize();
        }
    }

    public static void minimizeWindow() {
        if (driver.get() != null) {
            driver.get().manage().window().minimize();
        }
    }

    public static void setWindowSize(int width, int height) {
        if (driver.get() != null) {
            driver.get().manage().window().setSize(new org.openqa.selenium.Dimension(width, height));
        }
    }

    public static String getCurrentUrl() {
        return driver.get() != null ? driver.get().getCurrentUrl() : "";
    }

    public static String getPageTitle() {
        return driver.get() != null ? driver.get().getTitle() : "";
    }

    public static void navigateTo(String url) {
        if (driver.get() != null) {
            driver.get().get(url);
        }
    }

    public static void navigateBack() {
        if (driver.get() != null) {
            driver.get().navigate().back();
        }
    }

    public static void navigateForward() {
        if (driver.get() != null) {
            driver.get().navigate().forward();
        }
    }

    public static void refreshPage() {
        if (driver.get() != null) {
            driver.get().navigate().refresh();
        }
    }

    public static void switchToNewWindow() {
        if (driver.get() != null) {
            String originalWindow = driver.get().getWindowHandle();

            for (String windowHandle : driver.get().getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.get().switchTo().window(windowHandle);
                    break;
                }
            }
        }
    }

    public static void switchToMainWindow() {
        if (driver.get() != null) {
            String mainWindow = driver.get().getWindowHandles().iterator().next();
            driver.get().switchTo().window(mainWindow);
        }
    }

    public static void closeCurrentWindow() {
        if (driver.get() != null) {
            driver.get().close();
            switchToMainWindow();
        }
    }

    public static void acceptAlert() {
        if (driver.get() != null) {
            driver.get().switchTo().alert().accept();
        }
    }

    public static void dismissAlert() {
        if (driver.get() != null) {
            driver.get().switchTo().alert().dismiss();
        }
    }

    public static String getAlertText() {
        return driver.get() != null ? driver.get().switchTo().alert().getText() : "";
    }

    public static void waitForPageLoad() {
        getWait().until(d -> {
            String readyState = ((org.openqa.selenium.JavascriptExecutor) d)
                    .executeScript("return document.readyState").toString();
            return readyState.equals("complete");
        });
    }

    public static void executeJavaScript(String script, Object... args) {
        if (driver.get() != null) {
            ((org.openqa.selenium.JavascriptExecutor) driver.get()).executeScript(script, args);
        }
    }

    public static void scrollToElement(org.openqa.selenium.WebElement element) {
        executeJavaScript("arguments[0].scrollIntoView(true);", element);
    }

    public static void scrollToBottom() {
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public static void scrollToTop() {
        executeJavaScript("window.scrollTo(0, 0)");
    }

    public static void takeScreenshot(String testName) {
        ScreenshotUtils.capture(driver.get(), testName);
    }

    public static void clearCookies() {
        if (driver.get() != null) {
            driver.get().manage().deleteAllCookies();
        }
    }

    public static void clearLocalStorage() {
        executeJavaScript("window.localStorage.clear();");
    }

    public static void clearSessionStorage() {
        executeJavaScript("window.sessionStorage.clear();");
    }
}