package com.company.ecommerce.listeners;

import com.company.ecommerce.reporters.AllureManager;
import com.company.ecommerce.utils.WebDriverManagerUtil;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.UUID;

/**
 * Allure 测试监听器
 */
public class AllureTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getName();
        String className = result.getTestClass().getName();

        // 开始步骤
        String uuid = UUID.randomUUID().toString();
        StepResult stepResult = new StepResult()
                .setName("开始测试: " + testName)
                .setStatus(Status.PASSED);
        Allure.getLifecycle().startStep(uuid, stepResult);

        // 添加测试信息
        Allure.label("testClass", className);
        Allure.label("testMethod", testName);

        // 添加参数信息
        Object[] parameters = result.getParameters();
        if (parameters.length > 0) {
            Allure.addAttachment("测试参数", "text/plain",
                    String.join(", ", parameters.toString()));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Allure.label("status", "passed");
        Allure.addAttachment("执行结果", "测试通过 ✓");

        // 结束步骤
        Allure.getLifecycle().stopStep();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Allure.label("status", "failed");

        // 添加错误信息
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            Allure.addAttachment("错误信息", "text/plain",
                    throwable.getMessage());
            Allure.addAttachment("堆栈跟踪", "text/plain",
                    getStackTrace(throwable));
        }

        // 添加截图
        try {
//            byte[] screenshot = WebDriverManagerUtil.takeScreenshot("失败截图");
//            if (screenshot.length > 0) {
//                Allure.addAttachment("失败截图", "image/png",
//                        new ByteArrayInputStream(screenshot), "png");
//            }
        } catch (Exception e) {
            // 忽略截图错误
        }

        // 结束步骤
        Allure.getLifecycle().stopStep();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Allure.label("status", "skipped");

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            Allure.addAttachment("跳过原因", "text/plain",
                    throwable.getMessage());
        }

        Allure.getLifecycle().stopStep();
    }

    @Override
    public void onStart(ITestContext context) {
        AllureManager.startTestSuite(context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        // 生成测试统计
        Allure.addAttachment("测试套件统计", "text/plain",
                String.format("""
                测试套件: %s
                总测试数: %d
                通过: %d
                失败: %d
                跳过: %d
                通过率: %.2f%%
                """,
                        context.getName(),
                        context.getPassedTests().size() +
                                context.getFailedTests().size() +
                                context.getSkippedTests().size(),
                        context.getPassedTests().size(),
                        context.getFailedTests().size(),
                        context.getSkippedTests().size(),
                        (context.getPassedTests().size() * 100.0 /
                                (context.getPassedTests().size() +
                                        context.getFailedTests().size() +
                                        context.getSkippedTests().size()))
                )
        );
    }

    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("    at ").append(element).append("\n");
        }
        return sb.toString();
    }
}