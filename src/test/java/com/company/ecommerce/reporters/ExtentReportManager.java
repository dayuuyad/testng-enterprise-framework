package com.company.ecommerce.reporters;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.company.ecommerce.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Extent Reports 管理类
 * 用于生成美观的 HTML 测试报告
 */
public class ExtentReportManager {

    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static ExtentReports extent;
    private static Map<String, ExtentTest> testMap = new HashMap<>();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static String reportPath;

    private ExtentReportManager() {
        // 私有构造器，工具类
    }

    /**
     * 初始化 Extent Reports
     */
    public static void initReport() {
        if (extent != null) {
            logger.warn("Extent Reports 已经初始化");
            return;
        }

        try {
            // 创建报告目录
            String reportDir = ConfigManager.getInstance().getProperty("report.base.dir", "test-results/html-reports");
            String timestamp = DATE_FORMAT.format(new Date());
            String reportName = ConfigManager.getInstance().getProperty("report.name", "TestReport") + "_" + timestamp;

            reportPath = reportDir + File.separator + reportName + File.separator + "index.html";
            File reportFile = new File(reportPath);

            // 创建目录
            reportFile.getParentFile().mkdirs();

            // 创建 ExtentSparkReporter
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFile);

            // 配置报告
            configureReporter(sparkReporter);

            // 创建 ExtentReports 实例
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // 设置系统信息
            setSystemInfo();

            logger.info("✅ Extent Reports 初始化完成");
            logger.info("报告路径: {}", reportPath);

        } catch (Exception e) {
            logger.error("❌ Extent Reports 初始化失败", e);
            throw new RuntimeException("Failed to initialize Extent Reports", e);
        }
    }

    /**
     * 配置报告格式
     */
    private static void configureReporter(ExtentSparkReporter sparkReporter) {
        String theme = ConfigManager.getInstance().getProperty("report.theme", "DARK").toUpperCase();
        String documentTitle = ConfigManager.getInstance().getProperty("report.document.title", "Test Automation Report");
        String reportName = ConfigManager.getInstance().getProperty("report.name", "Test Execution Report");

        // 设置主题
        if ("STANDARD".equals(theme)) {
            sparkReporter.config().setTheme(Theme.STANDARD);
        } else {
            sparkReporter.config().setTheme(Theme.DARK);
        }

        // 设置编码
        sparkReporter.config().setEncoding("UTF-8");

        // 设置文档标题
        sparkReporter.config().setDocumentTitle(documentTitle);

        // 设置报告名称
        sparkReporter.config().setReportName(reportName);

        // 设置时间戳格式
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        // 设置 CSS
        sparkReporter.config().setCss(getCustomCSS());

        // 设置 JS
        sparkReporter.config().setJs(getCustomJS());

        // 启用时间线视图
        sparkReporter.config().setTimelineEnabled(true);

        // 设置离线模式
        sparkReporter.config().setOfflineMode(true);
    }

    /**
     * 设置系统信息
     */
    private static void setSystemInfo() {
        if (extent == null) {
            return;
        }

        // 基本系统信息
        extent.setSystemInfo("操作系统", System.getProperty("os.name"));
        extent.setSystemInfo("操作系统版本", System.getProperty("os.version"));
        extent.setSystemInfo("系统架构", System.getProperty("os.arch"));
        extent.setSystemInfo("Java 版本", System.getProperty("java.version"));
        extent.setSystemInfo("用户", System.getProperty("user.name"));
        extent.setSystemInfo("时区", System.getProperty("user.timezone"));

        // 项目信息
        extent.setSystemInfo("项目名称", ConfigManager.getInstance().getProperty("report.project.name", "Test Automation Framework"));
        extent.setSystemInfo("项目版本", ConfigManager.getInstance().getProperty("report.project.version", "1.0.0"));
        extent.setSystemInfo("环境", ConfigManager.getInstance().getEnvironmentName());
        extent.setSystemInfo("应用URL", ConfigManager.getInstance().getAppUrl());
        extent.setSystemInfo("API URL", ConfigManager.getInstance().getApiBaseUrl());
        extent.setSystemInfo("浏览器", ConfigManager.getInstance().getBrowserName());
        extent.setSystemInfo("执行时间", DISPLAY_DATE_FORMAT.format(new Date()));

        // Maven 信息
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null) {
            extent.setSystemInfo("Maven Home", mavenHome);
        }
    }

    /**
     * 设置额外的系统信息
     */
    public static void setSystemInfo(String key, String value) {
        if (extent != null && key != null && value != null) {
            extent.setSystemInfo(key, value);
        }
    }

    /**
     * 设置浏览器信息
     */
    public static void setBrowserInfo(String browser) {
        setSystemInfo("浏览器", browser);
    }

    /**
     * 创建测试节点
     */
    public static void createTest(String testName) {
        if (extent == null) {
            logger.warn("Extent Reports 未初始化，跳过创建测试: {}", testName);
            return;
        }

        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
        testMap.put(getCurrentThreadKey(), extentTest);

        logger.debug("创建测试节点: {}", testName);
    }

    /**
     * 创建带有描述的测试节点
     */
    public static void createTest(String testName, String description) {
        if (extent == null) {
            logger.warn("Extent Reports 未初始化，跳过创建测试: {}", testName);
            return;
        }

        ExtentTest extentTest = extent.createTest(testName, description);
        test.set(extentTest);
        testMap.put(getCurrentThreadKey(), extentTest);
    }

    /**
     * 设置测试描述
     */
    public static void setDescription(String description) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.getModel().setDescription(description);
        }
    }

    /**
     * 分配测试类别
     */
    public static void assignCategory(String... categories) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && categories != null) {
            for (String category : categories) {
                extentTest.assignCategory(category);
            }
        }
    }

    /**
     * 分配测试组
     */
    /**
     * 分配测试组 - 修正版本
     */
    public static void assignGroups(String... groups) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && groups != null) {
            // 正确的方式：直接调用 extentTest 的方法
            for (String group : groups) {
                extentTest.assignCategory(group); // 使用 assignCategory 来模拟 groups
            }

            // 或者使用更合适的方式，如果 ExtentReports 支持 groups
            // 实际上，ExtentReports 通常用 Category 来替代 Groups
            logger.debug("为测试分配组: {}", Arrays.toString(groups));
        }
    }

    /**
     * 记录测试通过
     */
    public static void logPass(String message) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.PASS, MarkupHelper.createLabel(message, ExtentColor.GREEN));
            logger.debug("记录通过: {}", message);
        }
    }

    /**
     * 记录测试失败
     */
    public static void logFail(Throwable throwable) {
        logFail(throwable.getMessage(), throwable);
    }

    public static void logFail(String message, Throwable throwable) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            String fullMessage = message;
            if (throwable != null) {
                fullMessage += "\n" + getStackTrace(throwable);
            }
            extentTest.log(Status.FAIL, MarkupHelper.createLabel(fullMessage, ExtentColor.RED));
            logger.debug("记录失败: {}", message);
        }
    }

    /**
     * 记录测试跳过
     */
    public static void logSkip(String message) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.SKIP, MarkupHelper.createLabel(message, ExtentColor.ORANGE));
            logger.debug("记录跳过: {}", message);
        }
    }

    /**
     * 记录测试信息
     */
    public static void logInfo(String message) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.INFO, message);
            logger.debug("记录信息: {}", message);
        }
    }

    /**
     * 记录测试警告
     */
    public static void logWarning(String message) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.WARNING, MarkupHelper.createLabel(message, ExtentColor.YELLOW));
            logger.debug("记录警告: {}", message);
        }
    }

    /**
     * 记录异常详情
     */
    public static void logException(Throwable throwable) {
        if (throwable != null) {
            logInfo("异常详情:\n" + getStackTrace(throwable));
        }
    }

    /**
     * 添加截图到报告
     */
    public static void addScreenshot(String screenshotPath) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && screenshotPath != null) {
            try {
                File screenshotFile = new File(screenshotPath);
                if (screenshotFile.exists()) {
                    extentTest.addScreenCaptureFromPath(screenshotPath);
                    logger.debug("添加截图到报告: {}", screenshotPath);
                } else {
                    logger.warn("截图文件不存在: {}", screenshotPath);
                }
            } catch (Exception e) {
                logger.error("添加截图到报告失败", e);
            }
        }
    }

    /**
     * 添加截图到报告（带标题）
     */
    public static void addScreenshot(String screenshotPath, String title) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && screenshotPath != null) {
            try {
                File screenshotFile = new File(screenshotPath);
                if (screenshotFile.exists()) {
                    extentTest.addScreenCaptureFromPath(screenshotPath, title);
                    logger.debug("添加截图到报告: {} - {}", title, screenshotPath);
                }
            } catch (Exception e) {
                logger.error("添加截图到报告失败", e);
            }
        }
    }

    /**
     * 添加链接到报告
     */
    public static void addLink(String url, String text) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && url != null) {
            String link = String.format("<a href='%s' target='_blank'>%s</a>",
                    url, text != null ? text : url);
            extentTest.log(Status.INFO, link);
        }
    }

    /**
     * 添加代码片段到报告
     */
    public static void addCodeBlock(String code, String language) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && code != null) {
            extentTest.info(MarkupHelper.createCodeBlock(code, language));
        }
    }

    /**
     * 添加 JSON 到报告
     */
    public static void addJson(String json) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && json != null) {
            extentTest.info(MarkupHelper.createCodeBlock(json, "json"));
        }
    }

    /**
     * 添加表格到报告
     */
    public static void addTable(String[][] data, String... headers) {
        ExtentTest extentTest = test.get();
        if (extentTest != null && data != null) {
            extentTest.info(MarkupHelper.createTable(data, headers));
        }
    }

    /**
     * 设置测试作者
     */
    public static void setAuthor(String author) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.assignAuthor(author);
        }
    }

    /**
     * 设置测试设备
     */
    public static void setDevice(String device) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.assignDevice(device);
        }
    }

    /**
     * 获取当前测试节点
     */
    public static ExtentTest getCurrentTest() {
        return test.get();
    }

    /**
     * 结束测试
     */
    public static void endTest() {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            // 测试节点由 ExtentReports 自动管理
            test.remove();
            testMap.remove(getCurrentThreadKey());
        }
    }

    /**
     * 刷新报告（写入磁盘）
     */
    public static void flushReport() {
        if (extent != null) {
            try {
                extent.flush();
                logger.info("✅ 测试报告已生成");
                logger.info("📊 报告路径: {}", reportPath);

                // 生成报告统计
                generateReportStats();

            } catch (Exception e) {
                logger.error("❌ 刷新报告失败", e);
            }
        } else {
            logger.warn("Extent Reports 未初始化，无法刷新");
        }
    }

    /**
     * 生成报告统计
     */
    private static void generateReportStats() {
        if (extent != null) {
            try {
                Map<String, Object> stats = (Map<String, Object>) extent.getStats();
                logger.info("📈 测试报告统计:");
                logger.info("  总测试数: {}", stats.get("tests"));
                logger.info("  通过: {}", stats.get("passed"));
                logger.info("  失败: {}", stats.get("failed"));
                logger.info("  跳过: {}", stats.get("skipped"));
                logger.info("  通过率: {}%", stats.get("passPercentage"));

            } catch (Exception e) {
                logger.error("获取报告统计失败", e);
            }
        }
    }

    /**
     * 获取报告路径
     */
    public static String getReportPath() {
        return reportPath;
    }

    /**
     * 清理资源
     */
    public static void close() {
        if (extent != null) {
            try {
                flushReport();
                extent = null;
                testMap.clear();
                test.remove();
                logger.info("Extent Reports 资源已清理");
            } catch (Exception e) {
                logger.error("清理 Extent Reports 资源失败", e);
            }
        }
    }

    /**
     * 获取当前线程的键
     */
    private static String getCurrentThreadKey() {
        return Thread.currentThread().getName() + "_" + Thread.currentThread().getId();
    }

    /**
     * 获取异常堆栈跟踪
     */
    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            sb.append("Caused by: ").append(getStackTrace(cause));
        }

        return sb.toString();
    }

    /**
     * 自定义 CSS
     */
    private static String getCustomCSS() {
        return """
            .test-content { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
            .test-name { font-weight: bold; font-size: 16px; }
            .timestamp { color: #666; font-size: 12px; }
            .badge { padding: 3px 8px; border-radius: 12px; font-size: 12px; }
            .pass-badge { background-color: #28a745; color: white; }
            .fail-badge { background-color: #dc3545; color: white; }
            .skip-badge { background-color: #ffc107; color: #212529; }
            .info-badge { background-color: #17a2b8; color: white; }
            .nav-wrapper { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
            .logo { font-weight: bold; font-size: 20px; color: white !important; }
            """;
    }

    /**
     * 自定义 JavaScript
     */
    private static String getCustomJS() {
        return """
            // 添加自定义交互
            document.addEventListener('DOMContentLoaded', function() {
                // 添加复制功能
                addCopyButtons();
                // 添加搜索功能
                addSearchFunctionality();
                // 添加主题切换
                addThemeToggle();
            });
            
            function addCopyButtons() {
                // 为代码块添加复制按钮
                document.querySelectorAll('pre code').forEach(function(codeBlock) {
                    var copyButton = document.createElement('button');
                    copyButton.className = 'copy-button';
                    copyButton.textContent = '复制';
                    copyButton.style.cssText = 'position: absolute; right: 5px; top: 5px; padding: 2px 8px; background: #007bff; color: white; border: none; border-radius: 3px; cursor: pointer;';
                    copyButton.onclick = function() {
                        navigator.clipboard.writeText(codeBlock.textContent);
                        this.textContent = '已复制!';
                        var self = this;
                        setTimeout(function() {
                            self.textContent = '复制';
                        }, 2000);
                    };
                    
                    var pre = codeBlock.parentNode;
                    pre.style.position = 'relative';
                    pre.appendChild(copyButton);
                });
            }
            
            function addSearchFunctionality() {
                // 添加搜索框
                var searchContainer = document.createElement('div');
                searchContainer.innerHTML = '<input type="text" id="testSearch" placeholder="搜索测试..." style="padding: 5px; margin: 10px; width: 200px;">';
                document.querySelector('.nav-wrapper').appendChild(searchContainer);
                
                document.getElementById('testSearch').addEventListener('input', function(e) {
                    var searchText = e.target.value.toLowerCase();
                    document.querySelectorAll('.test-content').forEach(function(test) {
                        var testText = test.textContent.toLowerCase();
                        test.style.display = testText.includes(searchText) ? '' : 'none';
                    });
                });
            }
            
            function addThemeToggle() {
                var themeToggle = document.createElement('button');
                themeToggle.textContent = '切换主题';
                themeToggle.style.cssText = 'position: fixed; bottom: 20px; right: 20px; padding: 10px; background: #333; color: white; border: none; border-radius: 5px; cursor: pointer; z-index: 1000;';
                themeToggle.onclick = function() {
                    document.body.classList.toggle('light-theme');
                };
                document.body.appendChild(themeToggle);
            }
            """;
    }

    /**
     * 从 TestNG 结果创建测试节点
     */
    public static void createTestFromResult(ITestResult result) {
        if (result == null || extent == null) {
            return;
        }

        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String testName = result.getName();
        String description = result.getMethod().getDescription();

        if (description == null || description.isEmpty()) {
            // 尝试从注解获取描述
            org.testng.annotations.Test testAnnotation = method.getAnnotation(org.testng.annotations.Test.class);
            if (testAnnotation != null && !testAnnotation.description().isEmpty()) {
                description = testAnnotation.description();
            }
        }

        // 创建测试节点
        ExtentTest extentTest;
        if (description != null && !description.isEmpty()) {
            extentTest = extent.createTest(testName, description);
        } else {
            extentTest = extent.createTest(testName);
        }

        // 设置测试类作为类别
        String className = result.getTestClass().getName();
        extentTest.assignCategory(className.substring(className.lastIndexOf('.') + 1));

        // 设置测试组
//        String[] groups = result.getMethod().getGroups();
//        if (groups.length > 0) {
//            extentTest.getModel().setGroups(groups);
//        }

        // 设置作者（如果有相关注解）
//        if (method.isAnnotationPresent(org.testng.annotations.Author.class)) {
//            org.testng.annotations.Author authorAnnotation = method.getAnnotation(org.testng.annotations.Author.class);
//            extentTest.assignAuthor(authorAnnotation.value());
//        }

        test.set(extentTest);
        testMap.put(getCurrentThreadKey(), extentTest);
    }

    /**
     * 更新测试结果状态
     */
    public static void updateTestResult(ITestResult result) {
        ExtentTest extentTest = test.get();
        if (extentTest == null) {
            return;
        }

        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                extentTest.pass("测试通过");
                break;
            case ITestResult.FAILURE:
                extentTest.fail(result.getThrowable());
                break;
            case ITestResult.SKIP:
                extentTest.skip("测试跳过: " + (result.getThrowable() != null ?
                        result.getThrowable().getMessage() : "未知原因"));
                break;
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                extentTest.warning("部分测试通过");
                break;
        }
    }
}