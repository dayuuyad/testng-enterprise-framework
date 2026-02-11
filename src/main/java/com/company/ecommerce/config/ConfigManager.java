package com.company.ecommerce.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置管理器
 * 用于加载和管理环境配置
 */
public class ConfigManager {

    private static Properties properties = new Properties();
    private static String currentEnvironment = "qa";

    /**
     * 加载配置文件
     * @param environment 环境名称 (dev, qa, staging, production)
     */
    public static void loadConfig(String environment) {
        currentEnvironment = environment;
        String configFile = environment + ".properties";

        try (InputStream input = ConfigManager.class
                .getClassLoader()
                .getResourceAsStream("config/" + configFile)) {

            if (input == null) {
                throw new RuntimeException("Configuration file not found: " + configFile);
            }

            properties.clear();
            properties.load(input);

            // 使用系统属性覆盖配置
            properties.putAll(System.getProperties());

        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + configFile, e);
        }
    }

    /**
     * 获取配置属性
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // 常用属性便捷方法
    public static String getAppUrl() {
        return getProperty("app.url");
    }

    public static String getApiBaseUrl() {
        return getProperty("app.api.url");
    }

    public static String getBrowserName() {
        return getProperty("browser.name", "chrome");
    }

    public static boolean isHeadless() {
        return getBooleanProperty("browser.headless", false);
    }

    public static String getTestUserEmail() {
        return getProperty("test.user.email");
    }

    public static String getTestUserPassword() {
        return getProperty("test.user.password");
    }

    public static String getDbHost() {
        return getProperty("db.host");
    }

    public static String getDbName() {
        return getProperty("db.name");
    }

    public static String getDbUsername() {
        return getProperty("db.username");
    }

    public static String getDbPassword() {
        return getProperty("db.password");
    }

    public static int getTestTimeout() {
        return getIntProperty("test.timeout", 300);
    }

    public static int getTestRetryCount() {
        return getIntProperty("test.retry.count", 2);
    }

    public static boolean isScreenshotOnFailure() {
        return getBooleanProperty("test.screenshot.on.failure", true);
    }

    public static String getCurrentEnvironment() {
        return currentEnvironment;
    }

    public static String getEnvironmentName() {
        return getProperty("env.name", "Unknown");
    }

    /**
     * 设置配置属性（用于测试）
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

}
