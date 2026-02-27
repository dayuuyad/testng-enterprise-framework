package com.company.ecommerce.base;

import com.company.ecommerce.config.ConfigManager;
import com.company.ecommerce.listeners.AllureTestListener;
import com.company.ecommerce.reporters.AllureManager;
import com.company.ecommerce.utils.DatabaseManager;
import io.qameta.allure.SeverityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

@Listeners(AllureTestListener.class)
public abstract class BaseTest {

    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
//    protected WebDriver driver;
    protected DatabaseManager dbManager;
//    protected APIUtils apiUtils;
//    protected TestDataUtils testDataUtils;
    protected static String hmacSha1Algorithm = "HmacSHA1";
    protected static String secretKey;
    protected static String appId;
    protected static String serviceCode;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        String environment = System.getProperty("environment", "qa");
//        logger.info(environment);
        secretKey = ConfigManager.getInstance().getApiSecretKey();
        appId = ConfigManager.getInstance().getApiAppId();
        serviceCode = ConfigManager.getInstance().getServiceCode();

        logger.info("=========================================");
        logger.info("Starting Test Suite");
        logger.info("Environment: {}", ConfigManager.getInstance().getEnvironmentName());
        logger.info("App URL: {}", ConfigManager.getInstance().getWebBaseUrl());
        logger.info("API URL: {}", ConfigManager.getInstance().getApiBaseUrl());
        logger.info("=========================================");

        // 初始化 Allure
        if (ConfigManager.getInstance().getBooleanProperty("allure.enabled", true)) {
            AllureManager.initAllure();
//            AllureManager.startTestSuite(context.getSuite().getName());
        }
    }

    @BeforeTest(alwaysRun = true)
    public void testSetup() {
        logger.info("Setting up test environment...");
//        testDataUtils = new TestDataUtils();
    }

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        String className = this.getClass().getSimpleName();
        logger.info("Initializing test class: {}", className);

        // 设置 Allure 分类
        if (ConfigManager.getInstance().getBooleanProperty("allure.enabled", true)) {
            AllureManager.setFeature(className.replace("Tests", ""));
        }

        // 根据测试类需求初始化相应的工具
        initializeRequiredTools();
    }

    /**
     * 根据测试类需求初始化工具
     */
    private void initializeRequiredTools() {
        // 检查是否需要数据库
        if (requiresDatabase()) {
            logger.info("初始化数据库连接...");
            dbManager = new DatabaseManager();
            dbManager.connect();
        }

//        // 检查是否需要 API 工具
//        if (requiresAPI()) {
//            logger.info("初始化 API 工具...");
//            apiUtils = new APIUtils();
//        }
//
//        // 检查是否需要 WebDriver
//        if (requiresBrowser()) {
//            logger.info("初始化 WebDriver...");
//            // WebDriver 在 @BeforeMethod 中初始化
//        }
    }

    @BeforeMethod(alwaysRun = true)
    public void methodSetup(Method method) {
        String methodName = method.getName();
        logger.info("Starting test method: {}", methodName);

        // 设置 Allure 信息
        if (ConfigManager.getInstance().getBooleanProperty("allure.enabled", true)) {
            setupAllureForMethod(method);
        }

//        // 初始化 WebDriver（如果需要）
//        if (requiresBrowser()) {
//            driver = WebDriverManager.getDriver();
//            if (driver != null) {
//                logger.info("浏览器已初始化: {}", ConfigManager.getInstance().getBrowserName());
//            }
//        }
    }

    /**
     * 为测试方法设置 Allure 信息
     */
    private void setupAllureForMethod(Method method) {
        // 设置测试名称
        AllureManager.addStep("开始测试: " + method.getName());

        // 设置严重级别
        if (method.isAnnotationPresent(org.testng.annotations.Test.class)) {
            org.testng.annotations.Test testAnnotation = method.getAnnotation(org.testng.annotations.Test.class);

            // 设置描述
            if (!testAnnotation.description().isEmpty()) {
                AllureManager.setDescription(testAnnotation.description());
            }

            // 根据优先级设置严重级别
            if (testAnnotation.priority() == 1) {
                AllureManager.setSeverity(SeverityLevel.CRITICAL);
            } else if (testAnnotation.priority() <= 3) {
                AllureManager.setSeverity(SeverityLevel.BLOCKER);
            } else if (testAnnotation.priority() <= 5) {
                AllureManager.setSeverity(SeverityLevel.NORMAL);
            } else {
                AllureManager.setSeverity(SeverityLevel.MINOR);
            }

            // 设置测试组
            if (testAnnotation.groups().length > 0) {
                AllureManager.setStory(String.join(", ", testAnnotation.groups()));
            }
        }

        // 设置所有者
        AllureManager.setOwner("自动化测试团队");
    }

    @AfterMethod(alwaysRun = true)
    public void methodCleanup(ITestResult result) {
        String testName = result.getName();

        if (result.getStatus() == ITestResult.FAILURE) {
            // 失败时自动截图
//            String screenshotPath = ScreenshotUtils.captureOnFailure(result);
//            if (screenshotPath != null) {
//                logger.info("测试失败截图: {}", screenshotPath);
//                // 可以将路径添加到报告
//                ExtentReportManager.addScreenshot(screenshotPath);
//            }
        }

        // 处理测试结果
        processTestResult(result);

        // 清理资源
        cleanupResources();
    }

    /**
     * 处理测试结果
     */
    private void processTestResult(ITestResult result) {
        String testName = result.getName();

        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                logger.info("✅ 测试通过: {}", testName);
                AllureManager.addStep("测试通过: " + testName);
                break;

            case ITestResult.FAILURE:
                logger.error("❌ 测试失败: {}", testName, result.getThrowable());

//                // 失败时截图并添加到 Allure
//                if (driver != null && ConfigManager.getInstance().isScreenshotOnFailure()) {
//                    try {
//                        byte[] screenshot = AllureManager.addScreenshot(driver, "失败截图");
//                        if (screenshot.length > 0) {
//                            logger.info("失败截图已添加到报告");
//                        }
//                    } catch (Exception e) {
//                        logger.warn("截图失败: {}", e.getMessage());
//                    }
//                }

                // 添加错误信息到 Allure
                if (result.getThrowable() != null) {
                    AllureManager.addTextAttachment(
                            result.getThrowable().getMessage(),
                            "错误信息"
                    );
                }
                break;

            case ITestResult.SKIP:
                logger.warn("⏸️ 测试跳过: {}", testName);
                AllureManager.addStep("测试跳过: " + testName);
                break;
        }
    }

    /**
     * 清理资源
     */
    private void cleanupResources() {
//        // 关闭浏览器
//        if (driver != null) {
//            WebDriverManager.quitDriver();
//            driver = null;
//        }

        // 注意：不在这里关闭 dbManager 和 apiUtils，
        // 因为它们可能需要在多个测试方法间复用
    }

    @AfterClass(alwaysRun = true)
    public void classCleanup() {
        logger.info("Cleaning up test class: {}", this.getClass().getSimpleName());

        // 关闭数据库连接
        if (dbManager != null) {
            dbManager.disconnect();
            dbManager = null;
        }

//        // 关闭 API 工具
//        if (apiUtils != null) {
//            apiUtils.close();
//            apiUtils = null;
//        }
    }

    @AfterTest(alwaysRun = true)
    public void testCleanup() {
        logger.info("Cleaning up test resources...");
//        if (testDataUtils != null) {
//            testDataUtils.cleanup();
//        }
    }

    @AfterSuite(alwaysRun = true)
    public void globalCleanup() {
        logger.info("=========================================");
        logger.info("Test Suite Completed");
        logger.info("=========================================");
    }

    // ========== 工具需求检测方法 ==========

    /**
     * 检测是否需要数据库（子类可重写）
     */
    protected boolean requiresDatabase() {
        return false;
    }

//    /**
//     * 检测是否需要 API 工具（子类可重写）
//     */
//    protected boolean requiresAPI() {
//        return false;
//    }

//    /**
//     * 检测是否需要浏览器（子类可重写）
//     */
//    protected boolean requiresBrowser() {
//        return false;
//    }

    // ========== 便捷方法 ==========

    /**
     * 执行数据库查询的便捷方法
     */
    protected Object queryDatabase(String sql, Object... params) {
        if (dbManager == null) {
            throw new IllegalStateException("DatabaseManager 未初始化，请先设置 requiresDatabase() 返回 true");
        }
        return dbManager.queryForObject(sql, params);
    }

//    /**
//     * 执行 API 调用的便捷方法
//     */
//    protected Response callApi2(String method, String endpoint, Object body) {
//        if (apiUtils == null) {
//            throw new IllegalStateException("APIUtils 未初始化，请先设置 requiresAPI() 返回 true");
//        }
//
//        switch (method.toUpperCase()) {
//            case "GET":
//                return apiUtils.get(endpoint);
//            case "POST":
//                return apiUtils.post(endpoint, body);
//            case "PUT":
//                return apiUtils.put(endpoint, body);
//            case "DELETE":
//                return apiUtils.delete(endpoint);
//            case "PATCH":
//                return apiUtils.patch(endpoint, body);
//            default:
//                throw new IllegalArgumentException("不支持的 HTTP 方法: " + method);
//        }
//    }
//
//    /**
//     * Allure 步骤：调用 API
//     */
//    protected Response callApi(String method, String endpoint, Object body) {
//        return AllureManager.addManualStep(String.format("调用 %s API: %s", method, endpoint), () -> {
//            if (apiUtils == null) {
//                throw new IllegalStateException("APIUtils 未初始化");
//            }
//
//            switch (method.toUpperCase()) {
//                case "GET":
//                    return apiUtils.get(endpoint);
//                case "POST":
//                    return apiUtils.post(endpoint, body);
//                case "PUT":
//                    return apiUtils.put(endpoint, body);
//                case "DELETE":
//                    return apiUtils.delete(endpoint);
//                case "PATCH":
//                    return apiUtils.patch(endpoint, body);
//                default:
//                    throw new IllegalArgumentException("不支持的 HTTP 方法: " + method);
//            }
//        });
//    }

}