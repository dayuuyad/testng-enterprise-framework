// src/test/java/com/company/ecommerce/listeners/TestListener.java
package com.company.ecommerce.listeners;

import com.company.ecommerce.reporters.ExtentReportManager;
import com.company.ecommerce.utils.NotificationService;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class TestListener implements ITestListener, IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        System.out.println("即将执行: " + testMethod.getName());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (testResult.getStatus() == ITestResult.FAILURE) {
            System.out.println("执行失败: " + testResult.getName());
            // 发送失败通知
            NotificationService.sendFailureNotification(testResult);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("开始测试: " + result.getName());
        ExtentReportManager.createTest(result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("✓ 测试通过: " + result.getName());
        ExtentReportManager.logPass("测试通过");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("✗ 测试失败: " + result.getName());
//        ScreenshotUtils.capture(result.getName());
        ExtentReportManager.logFail(result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("- 测试跳过: " + result.getName());
        ExtentReportManager.logSkip("测试跳过");
    }
}