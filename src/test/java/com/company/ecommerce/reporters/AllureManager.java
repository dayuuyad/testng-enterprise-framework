package com.company.ecommerce.reporters;

import com.company.ecommerce.utils.JsonUtils;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.restassured.AllureRestAssured;
import io.qameta.allure.util.ResultsUtils;
import io.restassured.response.Response;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Supplier;


/**
 * Allure 报告工具类
 */
public class AllureManager {

    private static final Logger logger = LoggerFactory.getLogger(AllureManager.class);

    private AllureManager() {
        // 工具类，私有构造器
    }

    /**
     * 初始化 Allure 配置
     */
    public static void initAllure() {
        // 设置环境变量
        System.setProperty("allure.results.directory", "test-results/allure-results");
        System.setProperty("allure.report.directory", "test-results/allure-report");

        // 设置 Allure 环境信息
        setEnvironmentInfo();

        logger.info("✅ Allure 报告系统初始化完成");
    }

    /**
     * 设置环境信息
     */
    private static void setEnvironmentInfo() {
        try {
            // 创建环境文件
            String envContent = """
                project=Test Automation Framework
                version=1.0.0
                environment=QA
                browser=chrome
                java.version=21
                allure.version=2.24.0
                selenium.version=4.19.0
                testng.version=7.9.0
                """;

            Files.write(
                    Paths.get("test-results/allure-results/environment.properties"),
                    envContent.getBytes()
            );
        } catch (IOException e) {
            logger.warn("无法创建 Allure 环境文件", e);
        }
    }

    /**
     * 添加测试步骤
     */
    @Step("{stepDescription}")
    public static void addStep(String stepDescription) {
        // 使用 @Step 注解，方法体可以为空
        logger.debug("测试步骤: {}", stepDescription);
    }

    /**
     * 添加带有参数的测试步骤
     */
    @Step("{stepName}")
    public static void addStepWithParams(String stepName, Object... params) {
        logger.debug("测试步骤: {} - 参数: {}", stepName, params);
    }

    /**
     * 手动添加步骤（不使用注解）
     */
    public static void addManualStep(String stepName, Runnable action) {
        String uuid = UUID.randomUUID().toString();
        StepResult stepResult = new StepResult().setName(stepName);

        try {
            Allure.getLifecycle().startStep(uuid, stepResult);
            action.run();
            Allure.getLifecycle().updateStep(uuid, s -> s.setStatus(Status.PASSED));
        } catch (Throwable e) {
            Allure.getLifecycle().updateStep(uuid, s -> s
                    .setStatus(ResultsUtils.getStatus(e).orElse(Status.BROKEN))
                    .setStatusDetails(ResultsUtils.getStatusDetails(e).orElse(null)));
            throw e;
        } finally {
            Allure.getLifecycle().stopStep(uuid);
        }
    }


    /**
     * 手动添加步骤（有返回值）- 修复您的错误的关键方法
     */
    public static <T> T addManualStep(String stepName, Supplier<T> action) {
        String uuid = UUID.randomUUID().toString();
        StepResult stepResult = new StepResult().setName(stepName);

        try {
            Allure.getLifecycle().startStep(uuid, stepResult);
            T result = action.get();
            Allure.getLifecycle().updateStep(uuid, s -> s.setStatus(Status.PASSED));
            return result;
        } catch (Throwable e) {
            Allure.getLifecycle().updateStep(uuid, s -> s
                    .setStatus(Status.FAILED)
                    .setStatusDetails(ResultsUtils.getStatusDetails(e).orElse(null)));
            throw e;
        } finally {
            Allure.getLifecycle().stopStep(uuid);
        }
    }

    public static Response addManualStepWithLog(String stepName, Object request, Supplier<Response> action) {
        String uuid = UUID.randomUUID().toString();
        StepResult stepResult = new StepResult().setName(stepName);

        try {
            Allure.getLifecycle().startStep(uuid, stepResult);
            // 记录请求参数
            Allure.addAttachment("请求参数", "application/json",
                    JsonUtils.toPrettyJson(request), ".json");

            Response result = action.get();
            // 记录返回值
            Allure.addAttachment("返回结果", "application/json",
                    JsonUtils.toPrettyJson(result.asString()), ".json");
//                    JsonUtils.responseToPrettyJson(result.asString()), ".json");

            Allure.getLifecycle().updateStep(uuid, s -> s.setStatus(Status.PASSED));
            return result;
        } catch (Throwable e) {
            Allure.getLifecycle().updateStep(uuid, s -> s
                    .setStatus(Status.FAILED)
                    .setStatusDetails(ResultsUtils.getStatusDetails(e).orElse(null)));
            throw e;
        } finally {
            Allure.getLifecycle().stopStep(uuid);
        }
    }




    /**
     * 添加截图到报告
     */
    @Attachment(value = "截图: {screenshotName}", type = "image/png")
    public static byte[] addScreenshot(WebDriver driver, String screenshotName) {
        if (driver == null) {
            logger.warn("WebDriver 为空，无法截图");
            return new byte[0];
        }

        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                logger.info("📸 截图已添加到 Allure 报告: {}", screenshotName);
                return screenshot;
            }
        } catch (Exception e) {
            logger.error("截图失败", e);
        }

        return new byte[0];
    }

    /**
     * 添加截图（外部文件）
     */
    @Attachment(value = "截图: {screenshotName}", type = "image/png")
    public static byte[] addScreenshotFromFile(String filePath, String screenshotName) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            logger.info("📸 文件截图已添加到 Allure 报告: {}", screenshotName);
            return bytes;
        } catch (IOException e) {
            logger.error("读取截图文件失败", e);
            return new byte[0];
        }
    }

    /**
     * 添加文本附件
     */
    @Attachment(value = "文本附件: {attachmentName}", type = "text/plain")
    public static String addTextAttachment(String content, String attachmentName) {
        logger.debug("添加文本附件: {}", attachmentName);
        return content;
    }

    /**
     * 添加 JSON 附件
     */
    @Attachment(value = "JSON 数据: {attachmentName}", type = "application/json")
    public static String addJsonAttachment(String json, String attachmentName) {
        logger.debug("添加 JSON 附件: {}", attachmentName);
        return json;
    }

    /**
     * 添加 XML 附件
     */
    @Attachment(value = "XML 数据: {attachmentName}", type = "application/xml")
    public static String addXmlAttachment(String xml, String attachmentName) {
        logger.debug("添加 XML 附件: {}", attachmentName);
        return xml;
    }

    /**
     * 添加 CSV 附件
     */
    @Attachment(value = "CSV 数据: {attachmentName}", type = "text/csv")
    public static String addCsvAttachment(String csv, String attachmentName) {
        logger.debug("添加 CSV 附件: {}", attachmentName);
        return csv;
    }

    /**
     * 添加 HTML 附件
     */
    @Attachment(value = "HTML 内容: {attachmentName}", type = "text/html")
    public static String addHtmlAttachment(String html, String attachmentName) {
        logger.debug("添加 HTML 附件: {}", attachmentName);
        return html;
    }

    /**
     * 添加文件附件
     */
    @Attachment(value = "文件: {attachmentName}")
    public static byte[] addFileAttachment(byte[] fileContent, String attachmentName) {
        logger.debug("添加文件附件: {}", attachmentName);
        return fileContent;
    }

    /**
     * 添加链接
     */
    public static void addLink(String name, String url) {
        Allure.link(name, url);
        logger.debug("添加链接: {} -> {}", name, url);
    }

    /**
     * 添加问题链接
     */
    public static void addIssue(String issueId) {
        Allure.issue(issueId, "https://example.com/issue/" + issueId);
        logger.debug("添加问题链接: {}", issueId);
    }

    /**
     * 添加测试用例链接
     */
    public static void addTestCase(String testCaseId) {
        Allure.tms(testCaseId, "https://example.com/tms/" + testCaseId);
        logger.debug("添加测试用例链接: {}", testCaseId);
    }

    /**
     * 设置测试描述
     */
    public static void setDescription(String description) {
        Allure.description(description);
        logger.debug("设置测试描述: {}", description);
    }

    /**
     * 设置严重级别
     */
    public static void setSeverity(io.qameta.allure.SeverityLevel severity) {
        Allure.label("severity", severity.value());
        logger.debug("设置严重级别: {}", severity);
    }

    /**
     * 设置功能模块
     */
    public static void setFeature(String feature) {
        Allure.label("feature", feature);
        logger.debug("设置功能模块: {}", feature);
    }

    /**
     * 设置故事/场景
     */
    public static void setStory(String story) {
        Allure.label("story", story);
        logger.debug("设置故事/场景: {}", story);
    }

    /**
     * 设置 Epic
     */
    public static void setEpic(String epic) {
        Allure.label("epic", epic);
        logger.debug("设置 Epic: {}", epic);
    }

    /**
     * 设置所有者
     */
    public static void setOwner(String owner) {
        Allure.label("owner", owner);
        logger.debug("设置所有者: {}", owner);
    }

    /**
     * 开始测试套件
     */
    public static void startTestSuite(String suiteName) {
        Allure.label("suite", suiteName);
        logger.info("🚀 开始测试套件: {}", suiteName);
    }

    /**
     * 获取 Allure Rest Assured 过滤器
     */
    public static AllureRestAssured getAllureRestAssuredFilter() {
        return new AllureRestAssured();
    }

    /**
     * 清理 Allure 结果目录
     */
    public static void cleanupResults() {
        try {
            File resultsDir = new File("test-results/allure-results");
            if (resultsDir.exists()) {
                deleteDirectory(resultsDir);
                logger.info("清理 Allure 结果目录");
            }
        } catch (Exception e) {
            logger.error("清理 Allure 结果目录失败", e);
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }

    /**
     * 生成 Allure 报告（命令行）
     */
    public static void generateReport() {
        try {
            logger.info("生成 Allure 报告...");

            // 调用 Allure 命令行
            ProcessBuilder builder = new ProcessBuilder();

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows
                builder.command("cmd.exe", "/c", "allure generate test-results/allure-results -o test-results/allure-report --clean");
            } else {
                // Linux/Mac
                builder.command("bash", "-c", "allure generate test-results/allure-results -o test-results/allure-report --clean");
            }

            Process process = builder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("✅ Allure 报告生成成功");
                logger.info("报告路径: file://" + new File("test-results/allure-report/index.html").getAbsolutePath());
            } else {
                logger.error("❌ Allure 报告生成失败，退出码: {}", exitCode);
            }

        } catch (Exception e) {
            logger.error("生成 Allure 报告失败", e);
        }
    }

    /**
     * 打开 Allure 报告
     */
    public static void openReport() {
        try {
            logger.info("打开 Allure 报告...");

            ProcessBuilder builder = new ProcessBuilder();
            String reportPath = new File("test-results/allure-report/index.html").getAbsolutePath();

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows
                builder.command("cmd.exe", "/c", "start " + reportPath);
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // Mac
                builder.command("open", reportPath);
            } else {
                // Linux
                builder.command("xdg-open", reportPath);
            }

            builder.start();

        } catch (Exception e) {
            logger.error("打开 Allure 报告失败", e);
        }
    }
}